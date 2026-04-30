#!/usr/bin/env python3
"""
Collect all Component.translatable calls from Java source files using regex.
Outputs a Markdown table with keys, varargs flag, and usage locations.
"""

import os
import json
import re
import sys
from collections import defaultdict

def find_matching_paren(text, start):
    """
    Find the index of the closing parenthesis that matches the opening
    parenthesis at position `start`. Ignores parentheses inside string literals.
    """
    stack = 0
    i = start
    in_single_quote = False
    in_double_quote = False
    escape = False
    while i < len(text):
        c = text[i]
        # Handle escape sequences inside strings
        if escape:
            escape = False
        else:
            if c == '\\':
                escape = True
            elif c == '"' and not in_single_quote:
                in_double_quote = not in_double_quote
            elif c == "'" and not in_double_quote:
                in_single_quote = not in_single_quote
            elif not in_single_quote and not in_double_quote:
                if c == '(':
                    stack += 1
                elif c == ')':
                    stack -= 1
                    if stack == 0:
                        return i
        i += 1
    return -1   # No matching parenthesis found

def extract_first_arg_and_has_varargs(arg_str):
    """
    Given the argument string (everything between the parentheses of the call),
    extract the first argument and determine if there are more arguments.
    Ignores nested commas inside string literals.
    """
    first_arg = []
    i = 0
    in_single_quote = False
    in_double_quote = False
    escape = False
    # Find the end of the first argument: either a comma (not inside quotes) or the end of string
    while i < len(arg_str):
        c = arg_str[i]
        if escape:
            escape = False
        else:
            if c == '\\':
                escape = True
            elif c == '"' and not in_single_quote:
                in_double_quote = not in_double_quote
            elif c == "'" and not in_double_quote:
                in_single_quote = not in_single_quote
            elif not in_single_quote and not in_double_quote:
                if c == ',':
                    # End of first argument
                    break
                # Else accumulate character
                first_arg.append(c)
            else:
                # Inside string, just accumulate
                first_arg.append(c)
        i += 1

    # Determine if there are more arguments
    remaining = arg_str[i+1:].strip()
    has_varargs = bool(remaining) and not (remaining.startswith(')') or remaining == '')

    # Clean up the first argument: strip whitespace and possibly quotes
    first_arg_str = ''.join(first_arg).strip()
    # If the argument is a string literal, remove the quotes
    if (first_arg_str.startswith('"') and first_arg_str.endswith('"')) or \
       (first_arg_str.startswith("'") and first_arg_str.endswith("'")):
        first_arg_str = first_arg_str[1:-1]

    return first_arg_str, has_varargs

def get_class_name(file_path, source_root):
    """
    Convert a Java file path to a fully qualified class name,
    then remove the prefix "xland.mcmod.neospeedzero." if present.
    """
    rel_path = os.path.relpath(file_path, source_root)
    if rel_path.startswith('..'):
        # File outside source root – fallback to basename without extension
        class_name = os.path.splitext(os.path.basename(file_path))[0]
    else:
        # Replace separators with dots and strip .java
        class_name = rel_path.replace(os.sep, '.')[:-5]
    # Strip the required prefix
    prefix = "xland.mcmod.neospeedzero."
    class_name = class_name.removeprefix(prefix).removeprefix('src.main.java.' + prefix)
    return class_name

def scan_file(file_path, source_root):
    """
    Scan a single Java file for Component.translatable calls.
    Returns a list of tuples (key, has_varargs, location_string).
    """
    results = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}", file=sys.stderr)
        return results

    # Find all occurrences of "Component.translatable("
    pattern = re.compile(r'Component\.translatable\(')
    class_name = get_class_name(file_path, source_root)

    for match in pattern.finditer(content):
        start_pos = match.end() - 1  # position of '('
        end_pos = find_matching_paren(content, start_pos)
        if end_pos == -1:
            # Malformed call, skip
            continue

        # Extract the arguments substring (between parentheses)
        arg_str = content[start_pos+1:end_pos]
        key, has_varargs = extract_first_arg_and_has_varargs(arg_str)

        # Calculate line number
        line_no = content[:match.start()].count('\n') + 1
        location = f"{class_name}:{line_no}"
        results.append((key, has_varargs, location))

    return results

def collect_uses(source_root):
    """
    Walk the source root and collect all translatable calls.
    Returns a dict: key -> (has_varargs_any, set_of_locations)
    """
    uses_by_key = defaultdict(list)   # key -> list of (has_varargs, location)
    for root, dirs, files in os.walk(source_root):
        for file in files:
            if not file.endswith('.java'):
                continue
            file_path = os.path.join(root, file)
            for key, has_varargs, location in scan_file(file_path, source_root):
                uses_by_key[key].append((has_varargs, location))

    # Aggregate: determine if any call for a key has varargs, and collect unique locations
    aggregated = []
    for key, entries in uses_by_key.items():
        any_varargs = any(varargs for varargs, _ in entries)
        # Keep locations in order of first occurrence
        seen = set()
        unique_locations = []
        for _, loc in entries:
            if loc not in seen:
                seen.add(loc)
                unique_locations.append(loc)
        aggregated.append((key, any_varargs, unique_locations))

    # Sort by key for deterministic output
    aggregated.sort(key=lambda x: x[0])
    return aggregated

def format_markdown_table(data):
    """Format the aggregated data as a ~~Markdown table~~ JSON string."""
    # lines = [
    #     "| key | hasVarArgs | uses |",
    #     "|-----|------------|------|"
    # ]
    # for key, has_varargs, locations in data:
    #     key_esc = key.replace('|', '\\|')
    #     uses_str = '<br>'.join(locations)
    #     uses_esc = uses_str.replace('|', '\\|')
    #     lines.append(f"| `{key_esc}` | {str(has_varargs).lower()} | {uses_esc} |")
    # return "\n".join(lines)
    return json.dumps([{
        "key": key,
        "has_varargs": has_varargs,
        "locations": locations,
    } for key, has_varargs, locations in data], ensure_ascii=False, indent=2)

def main():
    if len(sys.argv) > 1:
        source_root = sys.argv[1]
    else:
        source_root = os.path.join(os.getcwd(), "src", "main", "java")
        if not os.path.isdir(source_root):
            print(f"Default source root '{source_root}' does not exist. Please provide a directory.", file=sys.stderr)
            sys.exit(1)

    print(f"Scanning {source_root} ...", file=sys.stderr)
    data = collect_uses(source_root)
    print(format_markdown_table(data))

if __name__ == '__main__':
    main()
