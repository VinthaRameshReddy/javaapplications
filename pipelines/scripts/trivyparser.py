import json
from pathlib import Path

INPUT_FILE = "trivy-report.json"
OUTPUT_FILE = "scan-report.html"

def generate_html(report):
    html = ['<html><head><title>Trivy Scan Report</title><style>']
    html.append("""
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { text-align: left; padding: 8px; border: 1px solid #ccc; }
        th { background-color: #f2f2f2; }
        .low { background-color: #e6f7ff; }
        .medium { background-color: #fff7e6; }
        .high { background-color: #ffe6e6; }
        .critical { background-color: #ffd6d6; font-weight: bold; }
    """)
    html.append('</style></head><body>')
    html.append('<h1>🔎 Trivy Image Vulnerability Report</h1>')

    for target in report.get("Results", []):
        html.append(f"<h2>🖥️ Target: {target.get('Target')}</h2>")
        html.append(f"<p><b>Type:</b> {target.get('Type')}</p>")
        html.append('<table><tr><th>ID</th><th>Package</th><th>Version</th><th>Severity</th><th>Description</th><th>References</th></tr>')

        for vuln in target.get("Vulnerabilities", []):
            severity = vuln["Severity"].lower()
            desc = vuln.get("Title") or vuln.get("Description") or "No description available"
            refs = '<br>'.join(f'<a href="{ref}" target="_blank">{ref}</a>' for ref in vuln.get("References", []))
            html.append(
                f'<tr class="{severity}">'
                f'<td>{vuln.get("VulnerabilityID")}</td>'
                f'<td>{vuln.get("PkgName")}</td>'
                f'<td>{vuln.get("InstalledVersion")}</td>'
                f'<td>{vuln.get("Severity")}</td>'
                f'<td>{desc[:100]}...</td>'
                f'<td>{refs}</td>'
                '</tr>'
            )
        html.append('</table>')

    html.append('</body></html>')
    return '\n'.join(html)


def main():
    data = json.loads(Path(INPUT_FILE).read_text())
    html_content = generate_html(data)
    Path(OUTPUT_FILE).write_text(html_content)
    print(f"✅ HTML report generated: {OUTPUT_FILE}")

if __name__ == "__main__":
    main()

