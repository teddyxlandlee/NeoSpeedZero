#!/usr/bin/env python3
"""
Replace Component.translatable calls with NeoSpeedTranslations factory calls.
Usage: python replace_translatable.py <source_root> <meta.json>
"""

import os
import sys
import json
import re

def find_matching_paren(text, start):
    """
    Return index of closing parenthesis matching the one at position `start`.
    Ignores parentheses inside string literals.
    """
    stack = 0
    i = start
    in_single = False
    in_double = False
    escape = False
    while i < len(text):
        c = text[i]
        if escape:
            escape = False
        else:
            if c == '\\':
                escape = True
            elif c == '"' and not in_single:
                in_double = not in_double
            elif c == "'" and not in_double:
                in_single = not in_single
            elif not in_single and not in_double:
                if c == '(':
                    stack += 1
                elif c == ')':
                    stack -= 1
                    if stack == 0:
                        return i
        i += 1
    return -1

def parse_first_arg_and_rest(arg_str):
    """
    Parse the argument list string and return:
      - first_arg_text (the source text of the first argument)
      - rest_args_text (the substring after the first argument, including the comma if any)
    Returns (first_arg, rest_args) where rest_args may be empty or start with a comma.
    """
    first_arg = []
    rest = []
    i = 0
    in_single = False
    in_double = False
    escape = False
    # Find the end of the first argument
    while i < len(arg_str):
        c = arg_str[i]
        if escape:
            escape = False
        else:
            if c == '\\':
                escape = True
            elif c == '"' and not in_single:
                in_double = not in_double
            elif c == "'" and not in_double:
                in_single = not in_single
            elif not in_single and not in_double:
                if c == ',':
                    # End of first argument, the rest is after the comma
                    rest = arg_str[i+1:]  # include the comma? Actually the comma is at i, we skip it
                    break
        first_arg.append(c)
        i += 1
    else:
        # No comma found, so there is no rest
        rest = ""

    first_arg_str = ''.join(first_arg).strip()
    rest_str = rest.strip()
    return first_arg_str, rest_str

def get_string_literal_value(literal_text):
    """
    If the literal_text is a Java string literal (starting with " or '), return the unquoted value.
    Otherwise return None.
    """
    if (literal_text.startswith('"') and literal_text.endswith('"')) or \
            (literal_text.startswith("'") and literal_text.endswith("'")):
        # Remove quotes and handle escapes? We'll just return the raw inside for lookup.
        # For simplicity, assume the string is simple and escapes are rare in keys.
        return literal_text[1:-1]
    return None

def load_meta(meta_path):
    """Load meta JSON and return dict mapping key -> (field, has_varargs)."""
    with open(meta_path, 'r', encoding='utf-8') as f:
        meta = json.load(f)
    mapping = {}
    for entry in meta:
        key = entry.get('key')
        field = entry.get('field')
        has_varargs = entry.get('has_varargs')
        if key and field is not None and has_varargs is not None:
            mapping[key] = (field, has_varargs)
        else:
            print(f"Warning: entry missing required fields: {entry}", file=sys.stderr)
    return mapping

def replace_in_file(file_path, mapping):
    """Process a single Java file, replace calls, return True if modified."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}", file=sys.stderr)
        return False

    modified = False
    # Regex to find all Component.translatable( occurrences
    pattern = re.compile(r'Component\.translatable\(')
    # We'll iterate over matches, but we must adjust positions after each replacement
    # To avoid complexity, we'll work on the string and rebuild with offsets.
    # Simpler: find all matches in a loop, but each replacement shifts positions.
    # We'll collect replacements (start, end, new_text) and apply in reverse order.
    replacements = []
    pos = 0
    while True:
        match = pattern.search(content, pos)
        if not match:
            break
        start = match.start()
        # Find the matching closing parenthesis
        open_paren = match.end() - 1
        close_paren = find_matching_paren(content, open_paren)
        if close_paren == -1:
            # Malformed, skip
            print(f"Warning: Malformed call at {file_path}:{content[:start].count(chr(10))+1}", file=sys.stderr)
            pos = start + 1
            continue
        end = close_paren + 1  # include the closing ')'

        # Extract the arguments substring (between parentheses)
        args_str = content[open_paren+1:close_paren]
        first_arg, rest_args = parse_first_arg_and_rest(args_str)

        # Get key value
        key_value = get_string_literal_value(first_arg)
        if key_value is None:
            print(f"Warning: First argument is not a string literal at {file_path}:{content[:start].count(chr(10))+1} – skipping", file=sys.stderr)
            pos = end
            continue

        if key_value not in mapping:
            print(f"Warning: Key '{key_value}' not found in meta at {file_path}:{content[:start].count(chr(10))+1} – skipping", file=sys.stderr)
            pos = end
            continue

        field, expects_varargs = mapping[key_value]

        # Determine if the call has additional arguments
        has_extra = bool(rest_args) and not rest_args.startswith(')')
        # Build replacement
        if expects_varargs:
            if has_extra:
                # Use createWithArgs with the remaining arguments
                new_call = f"NeoSpeedTranslations.{field}.createWithArgs({rest_args})"
            else:
                # No extra args, but factory expects varargs – call with empty varargs
                new_call = f"NeoSpeedTranslations.{field}.createWithArgs()"
        else:
            if has_extra:
                # Mismatch: NoArgs but call has extra arguments
                print(f"Warning: Mismatch at {file_path}:{content[:start].count(chr(10))+1} – key '{key_value}' expects no args but call has extra arguments. Skipping.", file=sys.stderr)
                pos = end
                continue
            else:
                new_call = f"NeoSpeedTranslations.{field}.create()"

        replacements.append((start, end, new_call))
        pos = end

    if replacements:
        # Apply replacements in reverse order to avoid offset issues
        new_content = content
        for start, end, new_text in reversed(replacements):
            new_content = new_content[:start] + new_text + new_content[end:]
        # Write back
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {file_path}")
            return True
        except Exception as e:
            print(f"Error writing {file_path}: {e}", file=sys.stderr)
            return False
    return False

def main():
    if len(sys.argv) != 3:
        print("Usage: python replace_translatable.py <source_root> <meta.json>", file=sys.stderr)
        sys.exit(1)

    source_root = sys.argv[1]
    meta_path = sys.argv[2]

    if not os.path.isdir(source_root):
        print(f"Error: source root '{source_root}' is not a directory.", file=sys.stderr)
        sys.exit(1)

    mapping = load_meta(meta_path)
    print(f"Loaded {len(mapping)} keys from meta.", file=sys.stderr)

    # Walk through all .java files
    modified_count = 0
    for root, dirs, files in os.walk(source_root):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                if replace_in_file(file_path, mapping):
                    modified_count += 1

    print(f"Done. Modified {modified_count} file(s).", file=sys.stderr)

if __name__ == '__main__':
    main()