from generate_pack import *

def setup_directories(base_dir: Path):
    """创建必要目录"""
    os.makedirs(base_dir / SERVER_DIR, exist_ok=True)
    os.makedirs(base_dir / REPORTS_DIR, exist_ok=True)

def compare_items(version1, data1, version2, data2):
    """比较两个版本的物品差异"""
    results = {
        "added": {},      # 新增物品: {id: 翻译键}
        "removed": {},    # 删除物品: {id: 翻译键}
        "modified": {}    # 修改物品: {id: (旧翻译键, 新翻译键)}
    }
    if not isinstance(data1, dict):
        data1 = dict(data1)
    if not isinstance(data2, dict):
        data2 = dict(data2)
    
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

    if results['added'] or results['removed'] or results['modified']:
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
        for _id, key in results["added"].items():
            print(f"  - {_id}: {key} ({results['translations']['new'].get(key)})", file=output)
    
    # 输出删除物品
    if results["removed"]:
        print(f"\n🔴 删除物品 ({len(results['removed'])}):", file=output)
        for _id, key in results["removed"].items():
            print(f"  - {_id}: {key} ({results['translations']['old'].get(key)})", file=output)
    
    # 输出修改物品
    if results["modified"]:
        print(f"\n🟡 修改翻译键 ({len(results['modified'])}):", file=output)
        for _id, (old_key, new_key) in results["modified"].items():
            print(f"  - {_id}:")
            print(f"     旧: {old_key} ({results['translations']['old'].get(old_key)})", file=output)
            print(f"     新: {new_key} ({results['translations']['new'].get(new_key)})", file=output)
    
    # 统计总结
    total_changes = sum(len(v) for v in (results['added'], results['removed'], results['modified']))
    print(f"\n📊 总计变动: {total_changes} 项", file=output)

def main():
    parser = argparse.ArgumentParser(description="Minecraft物品报告对比工具")
    parser.add_argument("version1", help="第一个Minecraft版本号 (如 1.21.6)")
    parser.add_argument("version2", help="第二个Minecraft版本号 (如 1.21.7-rc2)")
    parser.add_argument('-o', '--output', help="报告输出路径 (可选)")
    parser.add_argument('-d', '--base', help="缓存路径（默认：当前目录）", default='.')

    args = parser.parse_args()
    
    try:
        base_dir = Path(args.base)
        setup_directories(base_dir=base_dir)
        
        # 处理版本1
        jar1 = download_server_jar(args.version1, base_dirname=base_dir)
        data1 = generate_item_report(args.version1, jar1, base_dirname=base_dir)

        # 处理版本2
        jar2 = download_server_jar(args.version2, base_dirname=base_dir)
        data2 = generate_item_report(args.version2, jar2, base_dirname=base_dir)

        # 比较并输出结果
        results = compare_items(args.version1, data1, args.version2, data2)
        print_comparison(results, args.version1, args.version2, args.output)
        
    except Exception as e:
        print(f"❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
