#!/usr/bin/env python3
"""Print per-package instruction coverage from a Kover XML report."""
import sys
import xml.etree.ElementTree as ET

REPORT_PATH = sys.argv[1] if len(sys.argv) > 1 else "build/reports/kover/report.xml"

root = ET.parse(REPORT_PATH).getroot()

pkgs = [
    (p.get("name"), int(c.get("covered")), int(c.get("missed")))
    for p in root.findall("package")
    for c in p.findall("counter")
    if c.get("type") == "INSTRUCTION"
]
pkgs.sort(key=lambda x: -x[2])

print(f"{'package':<55} {'cov%':>6} {'covered':>9} {'missed':>9} {'total':>9}")
for name, covered, missed in pkgs:
    total = covered + missed
    pct = (covered / total * 100) if total else 0
    print(f"{name:<55} {pct:6.1f} {covered:9d} {missed:9d} {total:9d}")

total_covered = sum(p[1] for p in pkgs)
total_missed = sum(p[2] for p in pkgs)
total = total_covered + total_missed
overall_pct = (total_covered / total * 100) if total else 0
print(f"\nOVERALL: {overall_pct:.2f}% ({total_covered}/{total} instructions, {total_missed} missed)")
