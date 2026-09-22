import os
import re
import base64
import markdown
import subprocess

def convert_md_to_pdf():
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    docs_dir = os.path.join(repo_root, "docs")
    md_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.md")
    html_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.html")
    pdf_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.pdf")

    print(f"[1/4] Reading Markdown source: {md_file}...")
    with open(md_file, "r", encoding="utf-8") as f:
        md_text = f.read()

    # Remove the raw mermaid code block from the printable HTML since Figure 0 architecture image is already displayed!
    md_text = re.sub(r'```mermaid[\s\S]*?```', '', md_text)

    # Convert simple inline and block LaTeX math into clean styled spans for printing
    md_text = re.sub(r'\$\$([\s\S]*?)\$\$', r'<div class="math-block"><em>\1</em></div>', md_text)
    md_text = re.sub(r'\$([^\$\n]+)\$', r'<span class="math-inline">\1</span>', md_text)

    # Convert Markdown to HTML
    print("[2/4] Parsing Markdown into structured HTML...")
    html_body = markdown.markdown(
        md_text,
        extensions=['extra', 'tables', 'toc', 'sane_lists']
    )

    # Base64 encode all local images into self-contained data URIs
    print("[3/4] Base64-encoding all embedded graphs and screenshots into HTML...")
    def encode_image(match):
        img_src = match.group(1)
        # resolve path
        if img_src.startswith("./"):
            local_path = os.path.join(docs_dir, img_src[2:])
        else:
            local_path = os.path.abspath(os.path.join(docs_dir, img_src))
        
        if os.path.exists(local_path):
            with open(local_path, "rb") as img_f:
                b64 = base64.b64encode(img_f.read()).decode("utf-8")
            ext = os.path.splitext(local_path)[1].lower().replace(".", "")
            if ext == "jpg": ext = "jpeg"
            return f'<img src="data:image/{ext};base64,{b64}"'
        else:
            print(f"Warning: image not found {local_path}")
            return match.group(0)

    html_body = re.sub(r'<img src="([^"]+)"', encode_image, html_body)

    # Wrap figures and captions into proper academic figure blocks
    html_body = re.sub(
        r'<p>(<img[^>]+>)</p>\s*<p><em>(Figure [^<]+)</em></p>',
        r'<div class="figure-container">\1<div class="figure-caption"><strong>\2</strong></div></div>',
        html_body
    )

    # Complete Academic Paper HTML Document with Print Styling
    full_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Zero Trust Architecture for IoT Networks - Project Report</title>
    <style>
        @page {{
            size: A4;
            margin: 18mm 15mm 20mm 15mm;
            @bottom-right {{
                content: counter(page);
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                font-size: 8pt;
                color: #64748b;
            }}
        }}

        body {{
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;
            font-size: 10pt;
            line-height: 1.55;
            color: #1e293b;
            background-color: #ffffff;
            margin: 0;
            padding: 0;
        }}

        h1 {{
            font-size: 17pt;
            font-weight: 800;
            color: #0f172a;
            text-align: center;
            line-height: 1.3;
            margin-top: 0;
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: -0.2px;
            border-bottom: 2.5px solid #3b82f6;
            padding-bottom: 14px;
        }}

        h2 {{
            font-size: 12pt;
            font-weight: 700;
            color: #1e3a8a;
            border-bottom: 1.5px solid #e2e8f0;
            padding-bottom: 4px;
            margin-top: 22px;
            margin-bottom: 8px;
            page-break-after: avoid;
        }}

        h3 {{
            font-size: 10.5pt;
            font-weight: 600;
            color: #334155;
            margin-top: 14px;
            margin-bottom: 6px;
            page-break-after: avoid;
        }}

        p {{
            margin-top: 0;
            margin-bottom: 8px;
            text-align: justify;
        }}

        /* Academic Metadata Box */
        ul:first-of-type {{
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-left: 4px solid #3b82f6;
            border-radius: 6px;
            padding: 10px 14px 10px 28px;
            margin-bottom: 16px;
            font-size: 9pt;
            color: #334155;
        }}

        ul:first-of-type li {{
            margin-bottom: 3px;
        }}

        /* Tables */
        table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 8.5pt;
            margin: 12px 0 16px 0;
            page-break-inside: avoid;
        }}

        th {{
            background-color: #1e3a8a;
            color: #ffffff;
            font-weight: 600;
            text-align: left;
            padding: 6px 8px;
            border: 1px solid #1e3a8a;
        }}

        td {{
            padding: 5px 8px;
            border: 1px solid #cbd5e1;
            color: #1e293b;
        }}

        tr:nth-child(even) td {{
            background-color: #f8fafc;
        }}

        /* Math Styling */
        .math-block {{
            background: #f1f5f9;
            border: 1px solid #e2e8f0;
            border-left: 3px solid #6366f1;
            padding: 8px 12px;
            margin: 10px 0;
            font-family: 'Cambria Math', 'Times New Roman', serif;
            font-size: 9.5pt;
            text-align: center;
            color: #1e1b4b;
        }}

        .math-inline {{
            font-family: 'Cambria Math', 'Times New Roman', serif;
            font-style: italic;
            font-weight: bold;
            color: #1e3a8a;
        }}

        /* Figures */
        .figure-container {{
            margin: 14px 0;
            text-align: center;
            page-break-inside: avoid;
        }}

        .figure-container img {{
            max-width: 95%;
            height: auto;
            border-radius: 6px;
            border: 1px solid #cbd5e1;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
        }}

        .figure-caption {{
            font-size: 8pt;
            color: #475569;
            margin-top: 5px;
            font-style: italic;
        }}

        /* Preformatted Code & Console Output */
        pre {{
            background: #0f172a;
            color: #f8fafc;
            padding: 8px 12px;
            border-radius: 4px;
            font-size: 8pt;
            line-height: 1.4;
            overflow-x: auto;
            margin: 10px 0;
            page-break-inside: avoid;
        }}

        code {{
            font-family: 'Consolas', 'Courier New', monospace;
            background: #f1f5f9;
            padding: 1px 4px;
            border-radius: 3px;
            font-size: 8.5pt;
            color: #0f172a;
        }}

        pre code {{
            background: transparent;
            padding: 0;
            color: #e2e8f0;
        }}

        a {{
            color: #2563eb;
            text-decoration: none;
            font-weight: 500;
        }}

        a:hover {{
            text-decoration: underline;
        }}

        ol, ul {{
            padding-left: 20px;
            margin-top: 4px;
            margin-bottom: 8px;
        }}

        li {{
            margin-bottom: 4px;
            text-align: justify;
        }}

        hr {{
            border: 0;
            border-top: 1px solid #cbd5e1;
            margin: 16px 0;
        }}
    </style>
</head>
<body>
    {html_body}
</body>
</html>
"""

    with open(html_file, "w", encoding="utf-8") as f:
        f.write(full_html)
    print(f"Generated standalone HTML: {html_file}")

    # Generate PDF using Microsoft Edge Headless print-to-pdf
    edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    if not os.path.exists(edge_path):
        edge_path = r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"

    print(f"[4/4] Converting HTML to PDF via Edge Headless engine ({edge_path})...")
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--no-pdf-header-footer",
        f"--print-to-pdf={pdf_file}",
        f"file:///{html_file.replace(os.sep, '/')}"
    ]

    res = subprocess.run(cmd, capture_output=True, text=True)
    if os.path.exists(pdf_file) and os.path.getsize(pdf_file) > 1000:
        size_kb = os.path.getsize(pdf_file) / 1024
        print(f"SUCCESS: Generated PDF Report at {pdf_file} ({size_kb:.1f} KB)")
        return True
    else:
        print(f"Error during PDF generation: {res.stderr}")
        return False

if __name__ == "__main__":
    convert_md_to_pdf()
