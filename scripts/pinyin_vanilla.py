#!/usr/bin/env python3
"""
本地首字母审核服务
用法:
    python service.py --input input.json --cache cache.json --output output.json
"""

import argparse
import base64
import binascii
import csv
import json
import logging
import math
import os
import threading
import sys
from typing import Dict, Optional, List, Any

try:
    from flask import Flask, jsonify, request, render_template_string
except ImportError:
    if __name__ == '__main__':
        print('Cannot find flask. Configure .venv first.')
        exit(-1)
    else:
        raise

# -------- 从 pinyin_suggestion 导入核心函数 --------
from pinyin_suggestion import get_first_letter_suggestion

# -------- Flask 应用 ----------
app = Flask(__name__)

_this_dir = os.path.dirname(sys.argv[0])
_logger = logging.getLogger('pinyin_vanilla')

if __name__ == '__main__':
    logging.basicConfig(level=os.environ.get('PINYIN_VANILLA_LOG_LEVEL', 'INFO'), format='%(asctime)s [%(levelname)s/%(name)s] %(message)s')

def _read_input_data(path: str) -> Dict[str, str]:
    with open(path, encoding='utf-8') as f:
        reader = csv.reader(f)
        if len(next(reader, ())) != 4:
            raise ValueError('Column count of the CSV file is not 4')
        return {item_id: chinese_name for _, item_id, _, chinese_name in reader}


class _accumulator:
    @staticmethod
    def percentage_map(total: int) -> dict[int, int]:   # value to percentage
        ret: dict[int, int] = {}
        for index, value in enumerate((math.ceil(total * i / 100) for i in range(1, 100)), start=1):
            ret[value] = index  # override with larger value
        ret[total] = 100
        return ret

    def __init__(self, name: str, total: int, logger: logging.Logger):
        self.name = name
        self.total = total
        self._logger = logger
        self._percentage_map = self.percentage_map(total)
        self._current = 0

    def mark(self):
        self._current += 1
        if self._current in self._percentage_map:
            percentage = self._percentage_map[self._current]
            self._logger.info('[acc %s] Progress: %s/%s (%s%%)', self.name, self._current, self.total, percentage)


# 全局状态
class ServiceState:
    def __init__(self, input_path: str, cache_path: str, output_path: str,
                 ai_model_name: str | None = None, ai_base_url: str | None = None,
                 ai_api_key: str | None = None):
        self.input_path = input_path
        self.cache_path = cache_path
        self.output_path = output_path
        self._ai_model_name = ai_model_name
        self._ai_base_url = ai_base_url
        self._ai_api_key = ai_api_key

        with open(input_path, 'r', encoding='utf-8') as f:
            self.input_data: Dict[str, str] = _read_input_data(input_path)
        self.total = len(self.input_data)

        self.cache: Dict[str, Dict[str, Any]] = {}
        self.determined = 0     # placeholder
        self._load_cache()

        self.determined = sum(1 for v in self.cache.values() if v.get('status') == 'determined')
        self.lock = threading.Lock()

    def _load_cache(self):
        """加载缓存，若缺失则自动处理。"""
        try:
            with open(self.cache_path, 'r', encoding='utf-8') as f:
                self.cache = json.load(f)
            missing = set(self.input_data.keys()) - set(self.cache.keys())
            if missing:
                self._process_missing(missing)
        except FileNotFoundError:
            self.cache = {}
            _logger.info('Generating cache...')
            self._process_all()

    def _process_all(self):
        acc = _accumulator('ServiceState', total=len(self.input_data), logger=_logger)
        for id_, text in self.input_data.items():
            self._process_one(id_, text)
            acc.mark()
        self._save_cache()

    def _process_missing(self, missing_ids):
        for id_ in missing_ids:
            text = self.input_data.get(id_)
            if text is not None:
                self._process_one(id_, text)
        self._save_cache()

    def _process_one(self, id_: str, text: str):
        try:
            readings, suggested, reason = get_first_letter_suggestion(
                text,
                model_name=self._ai_model_name, ai_base_url=self._ai_base_url,
                ai_api_key=self._ai_api_key,
            )
            if reason:
                _logger.debug('AI suggested %s: %s', id_, suggested)
        except Exception as e:
            self.cache[id_] = {
                'text': text,
                'readings': [],
                'suggested': None,
                'reason': f"处理出错: {str(e)}",
                'status': 'pending',
                'choice': None,
                'error': str(e)
            }
            return

        if reason is None and suggested is not None:
            self.cache[id_] = {
                'text': text,
                'readings': list(readings),
                'suggested': suggested,
                'reason': None,
                'status': 'determined',
                'choice': suggested
            }
            self.determined += 1
        else:
            self.cache[id_] = {
                'text': text,
                'readings': list(readings),
                'suggested': suggested,
                'reason': reason or "AI 未能给出建议",
                'status': 'pending',
                'choice': None
            }

    def _save_cache(self):
        with open(self.cache_path, 'w', encoding='utf-8') as f:
            json.dump(self.cache, f, ensure_ascii=False, indent=2)

    def get_pending(self) -> List[Dict]:
        pending = []
        for id_, data in self.cache.items():
            if data.get('status') == 'pending':
                candidates = set()
                for pinyin in data.get('readings', []):
                    if pinyin and pinyin[0].isalpha():
                        candidates.add(pinyin[0].upper())
                if not candidates and data.get('suggested'):
                    candidates.add(data['suggested'])
                # 生成 Base64 URL-safe 编码的 ID
                encoded_id = base64.urlsafe_b64encode(id_.encode()).decode().rstrip('=')
                pending.append({
                    'id': id_,
                    'encoded_id': encoded_id,
                    'text': data['text'],
                    'readings': data.get('readings', []),
                    'suggested': data.get('suggested'),
                    'reason': data.get('reason', ''),
                    'candidates': sorted(list(candidates)),
                    'error': data.get('error')
                })
        return pending

    def get_final_mapping(self) -> Dict[str, str]:
        mapping = {}
        for id_, data in self.cache.items():
            if data.get('status') == 'determined' and data.get('choice'):
                mapping[id_] = data['choice']
        return mapping

    def is_all_determined(self) -> bool:
        return self.determined == self.total

    def submit_choice(self, encoded_id: str, choice: str) -> bool:
        """解码 encoded_id 并提交选择。"""
        # 补充填充
        padding = 4 - (len(encoded_id) % 4)
        if padding != 4:
            encoded_id += '=' * padding
        try:
            id_ = base64.urlsafe_b64decode(encoded_id).decode()
        except (ValueError, binascii.Error):
            return False

        with self.lock:
            if id_ not in self.cache:
                return False
            data = self.cache[id_]
            if data.get('status') == 'determined':
                return False
            data['status'] = 'determined'
            data['choice'] = choice.upper()
            self.determined += 1
            self._save_cache()
            return True

    def export_final(self):
        mapping = self.get_final_mapping()
        with open(self.output_path, 'w', encoding='utf-8') as f:
            json.dump(mapping, f, ensure_ascii=False, indent=2)
        return mapping


state: Optional[ServiceState] = None


# ---------- 路由 ----------
@app.route('/')
def index():
    """从同目录的 pinyin_frontend.html 读取模板并渲染。"""

    o = os.path.join(_this_dir, 'pinyin_frontend.html')
    try:
        with open(o, 'r', encoding='utf-8') as f:
            html_content = f.read()
    except FileNotFoundError:
        return "未找到 index.html 文件", 500
    return render_template_string(html_content)


@app.route('/api/status')
def api_status():
    if state is None:
        return jsonify({'error': '服务未初始化'}), 500
    return jsonify({
        'total': state.total,
        'determined': state.determined,
        'pending': state.total - state.determined,
        'all_done': state.is_all_determined()
    })


@app.route('/api/pending')
def api_pending():
    if state is None:
        return jsonify({'error': '服务未初始化'}), 500
    return jsonify(state.get_pending())


@app.route('/api/review/<encoded_id>', methods=['POST'])
def api_review(encoded_id):
    """接收 Base64 编码的 ID。"""
    if state is None:
        return jsonify({'error': '服务未初始化'}), 500
    data = request.get_json()
    if not data or 'choice' not in data:
        return jsonify({'error': '缺少 choice 字段'}), 400
    choice = data['choice'].strip().upper()
    if not choice or not choice.isalpha() or len(choice) != 1:
        return jsonify({'error': 'choice 必须是一个大写字母'}), 400

    success = state.submit_choice(encoded_id, choice)
    if not success:
        return jsonify({'error': '提交失败，ID无效或已确定'}), 400

    if state.is_all_determined():
        mapping = state.export_final()
        return jsonify({'status': 'ok', 'all_done': True, 'final': mapping})
    else:
        return jsonify({'status': 'ok', 'all_done': False})


@app.route('/api/final')
def api_final():
    if state is None:
        return jsonify({'error': '服务未初始化'}), 500
    return jsonify(state.get_final_mapping())


@app.route('/api/export', methods=['POST'])
def api_export():
    if state is None:
        return jsonify({'error': '服务未初始化'}), 500
    if not state.is_all_determined():
        return jsonify({'error': '尚有未确定的条目，无法导出'}), 400
    mapping = state.export_final()
    return jsonify({'status': 'exported', 'final': mapping})


# ---------- 启动 ----------
def main():
    parser = argparse.ArgumentParser(description='首字母审核服务')
    parser.add_argument('--input', required=True, help='输入 CSV 文件路径')
    parser.add_argument('--cache', required=True, help='缓存 JSON 文件路径')
    parser.add_argument('--output', required=True, help='最终输出 JSON 文件路径')
    parser.add_argument('--ai-model', required=False, help='自定义 AI 大模型')
    parser.add_argument('--ai-base-url', required=False, help='自定义 OpenAI 端点')
    args = parser.parse_args()

    global state
    state = ServiceState(
        args.input, args.cache, args.output,
        ai_model_name=args.ai_model, ai_base_url=args.ai_base_url,
        ai_api_key=os.environ.get('PINYIN_VANILLA_API_KEY')
    )

    # 启动服务（监听本地 60520 端口）
    app.run(host='127.0.0.1', port=60520, debug=False, threaded=True)


if __name__ == '__main__':
    main()