import json
from typing import Tuple, Optional
import re
import unicodedata
try:
    from pypinyin import pinyin, Style
    from openai import OpenAI
except ImportError:
    if __name__ == '__main__':
        print('pypinyin and openai not found. Please configure .venv first')
        exit(-1)
    else:
        raise


def _remove_tone(char: str) -> str:
    return unicodedata.normalize('NFKD', char)[0]


def get_first_letter_suggestion(
        input_str: str,
        model_name: str | None = None,
        ai_base_url: str | None = None,
        ai_api_key: str | None = None,
) -> Tuple[Tuple[str, ...], str, Optional[str]]:
    """
    返回输入字符串第一个字符的拼音列表、建议的大写首字母及原因（若使用大模型）。

    Args:
        input_str: 待判断字符串，首字符必须是 ASCII 字母或可被 pypinyin 识别的汉字（含扩展区）。
        model_name: 用于多音字判断的 Ollama 模型名称（推荐非推理模型）。
        ollama_base_url: Ollama 服务端点。

    Returns:
        (读音元组, 建议的大写首字母, reason或None)
        示例：
            "重新启动" -> (('chóng', 'zhòng'), 'C', '应为 "chóng"，表示重新开始')
            "TNT炸药"  -> (('T',), 'T', None)
            "场面"     -> (('cháng', 'chǎng'), 'C', None)
            "𰻞𰻞面"   -> (('biáng',), 'B', None)

    Raises:
        ValueError: 若首字符不是 ASCII 字母且 pypinyin 无法转换（非汉字）。
    """
    model_name = model_name or 'qwen3.5:4b'
    ai_base_url = ai_base_url or "http://localhost:11434/v1"

    if not input_str:
        raise ValueError("输入字符串不能为空")
    first_char = input_str[0]

    # 1. ASCII 字母直接返回
    if first_char.isascii() and first_char.isalpha():
        upper = _remove_tone(first_char.upper())
        return (upper,), upper, None

    # 2. 非 ASCII 字符 → 尝试用 pypinyin 转换
    raw_pinyins = pinyin(first_char, style=Style.TONE, heteronym=True, errors='ignore')
    if not raw_pinyins:
        raise ValueError(f"字符 '{first_char}' 不是有效的汉字且非 ASCII 字母")
    pinyins = raw_pinyins[0]

    # 3. 获取首字母集合
    initials = [_remove_tone(p[0].upper()) for p in pinyins]
    unique_initials = list(set(initials))

    # 4. 若所有读音首字母相同，无需大模型
    if len(unique_initials) == 1:
        return tuple(pinyins), unique_initials[0], None

    # 5. 首字母不同 → 调用大模型判断
    client = OpenAI(base_url=ai_base_url, api_key=ai_api_key or 'placeholder')

    # 强制要求 JSON 输出，并在 reason 中明确包含正确的拼音（带声调）
    prompt = (
        f"请根据上下文判断汉字“{first_char}”在词语“{input_str}”中的正确读音（拼音）。"
        f"这个字有多个读音：{', '.join(pinyins)}。"
        "请仅输出一个 JSON 对象，格式为："
        '{"initial": "大写首字母", "reason": "应为“正确拼音”，简短理由"}。'
        "不要输出其他任何内容。"
    )

    try:
        response = client.chat.completions.create(
            model=model_name,
            messages=[{"role": "user", "content": prompt}],
            temperature=0.1,
            extra_body={'think': False},
        )
        answer = response.choices[0].message.content.strip()

        # 尝试解析 JSON
        try:
            data = json.loads(answer)
            suggested_letter = data["initial"].strip().upper()
            reason = data["reason"].strip()
        except (json.JSONDecodeError, KeyError):
            # 若解析失败，降级为从文本中提取
            match = re.search(r'([A-Z])\s*[,，]\s*(.+?)(?:"|$)', answer)
            if match:
                suggested_letter = match.group(1)
                reason = match.group(2).strip()
            else:
                # 如果完全无法解析，采用默认
                suggested_letter = initials[0]
                reason = f"模型输出解析失败，采用默认读音 {pinyins[0]}"

    except Exception as e:
        suggested_letter = initials[0]
        reason = f"大模型调用失败 ({e})，采用默认读音 {pinyins[0]}"

    return tuple(pinyins), suggested_letter, reason
