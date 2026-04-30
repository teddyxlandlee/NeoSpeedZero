#!/usr/bin/env python3
"""
Generate NeoSpeedTranslations.java from a meta JSON file and en_us.json.
Usage: python generate_translations.py meta.json en_us.json > NeoSpeedTranslations.java
"""

import sys
import json
import re

def escape_java_string(s):
    """Escape a string for use as a Java string literal."""
    # Replace backslashes first
    s = s.replace('\\', '\\\\')
    # Replace double quotes
    s = s.replace('"', '\\"')
    # Replace newlines (if any)
    s = s.replace('\n', '\\n')
    s = s.replace('\r', '\\r')
    # Tab
    s = s.replace('\t', '\\t')
    return s

def main():
    if len(sys.argv) != 3:
        print("Usage: python generate_translations.py <meta.json> <en_us.json>", file=sys.stderr)
        sys.exit(1)

    meta_path = sys.argv[1]
    lang_path = sys.argv[2]

    # Load meta JSON
    try:
        with open(meta_path, 'r', encoding='utf-8') as f:
            meta = json.load(f)
    except Exception as e:
        print(f"Error reading meta JSON: {e}", file=sys.stderr)
        sys.exit(1)

    # Load en_us.json
    try:
        with open(lang_path, 'r', encoding='utf-8') as f:
            en_us = json.load(f)
    except Exception as e:
        print(f"Error reading en_us.json: {e}", file=sys.stderr)
        sys.exit(1)

    # Prepare output lines
    lines = [
        "package xland.mcmod.neospeedzero;",
        "",
        "import static xland.mcmod.neospeedzero.util.TranslatableComponentFactory.*;",
        "",
        "public interface NeoSpeedTranslations {",
    ]

    # Keep track of missing keys for warning
    missing_keys = []

    for entry in meta:
        key = entry.get('key')
        field_name = entry.get('field')
        has_varargs = entry.get('has_varargs')
        # Optional: locations are not used in output

        if not key or not field_name:
            print(f"Warning: entry missing key or field: {entry}", file=sys.stderr)
            continue

        # Look up fallback in en_us
        fallback = en_us.get(key)
        if fallback is None:
            missing_keys.append(key)
            fallback = ""  # empty fallback
            print(f"Warning: key '{key}' not found in en_us.json", file=sys.stderr)

        # Escape fallback
        fallback_escaped = escape_java_string(fallback)

        # Determine factory method
        if has_varargs:
            factory = "withArgs"
            type_name = "WithArgs"
        else:
            factory = "noArgs"
            type_name = "NoArgs"

        line = f"    {type_name} {field_name} = {factory}(\"{key}\", \"{fallback_escaped}\");"
        lines.append(line)

    lines.append("}")

    # If any keys were missing, we still output but warn
    if missing_keys:
        print(f"Warning: {len(missing_keys)} key(s) missing from en_us.json", file=sys.stderr)

    # Print the generated file
    sys.stdout.write("\n".join(lines))

if __name__ == "__main__":
    main()