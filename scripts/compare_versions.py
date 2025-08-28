import hashlib
import os
import json
import requests
import subprocess
import argparse
from collections import defaultdict
import shutil

from generate_pack import *

def setup_directories():
    """创建必要目录"""
    os.makedirs(SERVER_DIR, exist_ok=True)
    os.makedirs(REPORTS_DIR, exist_ok=True)

def compare_items(version1, data1, version2, data2):
    """比较两个版本的物品差异"""
    results = {
        "added": {},      # 新增物品: {id: 翻译键}
        "removed": {},    # 删除物品: {id: 翻译键}
        "modified": {}    # 修改物品: {id: (旧翻译键, 新翻译键)}
    }
    
    # 检测新增和修改
    for item_id, trans_key2 in data2.items():
        if item_id not in data1:
            results["added"][item_id] = trans_key2
        elif data1[item_id] != trans_key2:
            results["modified"][item_id] = (data1[item_id], trans_key2)
    
    # 检测删除
    for item_id, trans_key1 in data1.items():
        if item_id not in data2:
            results["removed"][item_id] = trans_key1

    if (results['added'] or results['removed'] or results['modified']):
        results['translations'] = {
            'old': load_chinese_translations(version1),
            'new': load_chinese_translations(version2)
        }
    
    return results

def print_comparison(results, version1, version2, output):
    """可视化输出比较结果"""
    if isinstance(output, str):
        with open(output, 'w', encoding='utf8') as f:
            print_comparison(results, version1, version2, f)
        return

    print(f"\n🔍 版本对比结果: {version1} → {version2}", file=output)
    
    # 输出新增物品
    if results["added"]:
        print(f"\n🟢 新增物品 ({len(results['added'])}):", file=output)
        for id, key in results["added"].items():
            print(f"  - {id}: {key} ({results['translations']['new'].get(key)})", file=output)
    
    # 输出删除物品
    if results["removed"]:
        print(f"\n🔴 删除物品 ({len(results['removed'])}):", file=output)
        for id, key in results["removed"].items():
            print(f"  - {id}: {key} ({results['translations']['old'].get(key)})", file=output)
    
    # 输出修改物品
    if results["modified"]:
        print(f"\n🟡 修改翻译键 ({len(results['modified'])}):", file=output)
        for id, (old_key, new_key) in results["modified"].items():
            print(f"  - {id}:")
            print(f"     旧: {old_key} ({results['translations']['old'].get(key)})", file=output)
            print(f"     新: {new_key} ({results['translations']['new'].get(key)})", file=output)
    
    # 统计总结
    total_changes = sum(len(v) for v in (results['added'], results['removed'], results['modified']))
    print(f"\n📊 总计变动: {total_changes} 项", file=output)

def main():
    parser = argparse.ArgumentParser(description="Minecraft物品报告对比工具")
    parser.add_argument("version1", help="第一个Minecraft版本号 (如 1.21.6)")
    parser.add_argument("version2", help="第二个Minecraft版本号 (如 1.21.7-rc2)")
    parser.add_argument('-o', '--output', help="报告输出路径 (可选)")
    args = parser.parse_args()
    
    try:
        setup_directories()
        
        # 处理版本1
        jar1 = download_server_jar(args.version1)
        report1_path = generate_item_report(args.version1, jar1)
        data1 = load_item_data(report1_path)
        
        # 处理版本2
        jar2 = download_server_jar(args.version2)
        report2_path = generate_item_report(args.version2, jar2)
        data2 = load_item_data(report2_path)
        
        # 比较并输出结果
        results = compare_items(args.version1, data1, args.version2, data2)
        print_comparison(results, args.version1, args.version2, args.output)
        
    except Exception as e:
        print(f"❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
