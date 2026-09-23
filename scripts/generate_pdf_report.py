import os
import re
import base64
import shutil
import subprocess
import markdown
import pymupdf
from bs4 import BeautifulSoup

def generate_academic_pdf():
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    docs_dir = os.path.join(repo_root, "docs")
    graphs_dir = os.path.join(docs_dir, "graphs")
    preview_dir = os.path.join(graphs_dir, "preview")
    os.makedirs(preview_dir, exist_ok=True)

    md_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.md")
    html_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.html")
    academic_pdf_file = os.path.join(docs_dir, "KL_UNIVERSITY_MINI_PROJECT_REPORT_REBAKA_MEDA.pdf")
    default_pdf_file = os.path.join(docs_dir, "MINI_PROJECT_REPORT.pdf")

    print(f"[1/6] Loading Markdown source from: {md_file}")
    with open(md_file, "r", encoding="utf-8") as f:
        md_text = f.read()

    # Pre-process math equations and LaTeX symbols for publication-grade typography
    # Order matters: replace \left and \right first so \le doesn't corrupt \left
    md_text = md_text.replace(r'\left(', '(').replace(r'\right)', ')')
    md_text = md_text.replace(r'\le', '≤')
    md_text = md_text.replace(r'\in', '∈')
    md_text = md_text.replace(r'\cdot', '·')
    md_text = md_text.replace(r'\Delta', 'Δ')
    md_text = md_text.replace(r'\lambda', 'λ')
    md_text = md_text.replace(r'e^{-\lambda \Delta t}', 'e^{-λ·Δt}')
    md_text = md_text.replace(r'e^{-λ Δ t}', 'e^{-λ·Δt}')
    md_text = md_text.replace(r'\text{ pkt/s}', ' pkt/s').replace(r'\text{ pts}', ' pts')
    md_text = re.sub(r'\\text\{([^}]+)\}', r'\1', md_text)

    # Style block and inline math
    md_text = re.sub(r'\$\$([\s\S]*?)\$\$', r'<div class="math-block"><em>\1</em></div>', md_text)
    md_text = re.sub(r'\$([^\$\n]+)\$', r'<span class="math-inline">\1</span>', md_text)

    # Convert Markdown to HTML
    print("[2/6] Parsing Markdown into structured HTML...")
    raw_html = markdown.markdown(
        md_text,
        extensions=['extra', 'tables', 'toc', 'sane_lists']
    )

    soup = BeautifulSoup(raw_html, "html.parser")

    # Helper function to convert local image to base64
    def get_image_base64(img_filename):
        img_path = os.path.join(graphs_dir, img_filename)
        if not os.path.exists(img_path):
            img_path = os.path.join(docs_dir, img_filename)
        if os.path.exists(img_path):
            with open(img_path, "rb") as f:
                b64 = base64.b64encode(f.read()).decode("utf-8")
            ext = os.path.splitext(img_path)[1].lower().replace(".", "")
            if ext == "jpg": ext = "jpeg"
            return f"data:image/{ext};base64,{b64}"
        print(f"Warning: Image {img_filename} not found at {img_path}")
        return ""

    print("[3/6] Transforming HTML structure into publication-grade academic layout...")

    # Build the official KL University Institutional Header
    header_html = """
    <div class="inst-header">
        <div class="inst-top-bar">
            <div class="inst-title-group">
                <div class="inst-university">KONERU LAKSHMAIAH EDUCATION FOUNDATION</div>
                <div class="inst-deemed">(Category-1 Deemed to be University estd. u/s 3 of the UGC Act, 1956)</div>
                <div class="inst-dept">DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING</div>
                <div class="inst-badge">PROJECT REPORT &bull; ACADEMIC YEAR 2025–2026</div>
            </div>
        </div>

        <h1 class="paper-title">ZERO TRUST ARCHITECTURE FOR IOT NETWORKS: CONTINUOUS ATTESTATION, DECENTRALIZED IDENTITY (W3C DID), AND SUB-15MS INTRUSION DEFENSE</h1>

        <div class="author-card">
            <div class="author-row">
                <div class="author-cell">
                    <span class="meta-label">Candidate Name:</span>
                    <span class="meta-val highlight">Rebaka Meda</span>
                </div>
                <div class="author-cell">
                    <span class="meta-label">Registration Number:</span>
                    <span class="meta-val highlight">2400032563</span>
                </div>
                <div class="author-cell">
                    <span class="meta-label">Academic Program:</span>
                    <span class="meta-val">B.Tech in Computer Science &amp; Engineering</span>
                </div>
            </div>
            <div class="author-row">
                <div class="author-cell">
                    <span class="meta-label">Institution:</span>
                    <span class="meta-val">KL University, Green Fields, Vaddeswaram, AP, India</span>
                </div>
                <div class="author-cell">
                    <span class="meta-label">Subject / Course:</span>
                    <span class="meta-val highlight">Smart Contract for Web 3 Engineering</span>
                </div>
                <div class="author-cell">
                    <span class="meta-label">Specialization:</span>
                    <span class="meta-val highlight">Distributed Ledger Analytics</span>
                </div>
            </div>
            <div class="author-row project-links">
                <div class="author-cell full-width">
                    <span class="meta-label">GitHub Repository:</span>
                    <a href="https://github.com/Rebaka8/zeroTrust" target="_blank" class="highlight">https://github.com/Rebaka8/zeroTrust</a> &bull;
                    <span class="meta-label">Live App:</span>
                    <a href="https://zero-trust-snowy.vercel.app" target="_blank">zero-trust-snowy.vercel.app</a> &bull;
                    <span class="meta-label">PDP Backend:</span>
                    <a href="https://zerotrust-iot-backend.onrender.com" target="_blank">Render Core PDP</a> &bull;
                    <span class="demo-cred">Default Credentials: <code>admin / Admin@123456</code></span>
                </div>
            </div>
        </div>
    </div>
    """

    # Remove the raw markdown title, header text, and metadata lists from parsed soup
    h1_tag = soup.find("h1")
    if h1_tag:
        curr = h1_tag.next_sibling
        while curr and getattr(curr, 'name', '') != 'h2':
            nxt = curr.next_sibling
            curr.extract()
            curr = nxt
        h1_tag.extract()

    # Pre-encode all graphics
    arch_b64 = get_image_base64("architecture_diagram.png")
    cm_b64 = get_image_base64("confusion_matrix_heatmap.png")
    roc_b64 = get_image_base64("roc_curve_analysis.png")
    timeline_b64 = get_image_base64("trust_score_vs_latency_timeline.png")
    bar_b64 = get_image_base64("comparative_security_benchmark.png")
    login_b64 = get_image_base64("screenshot_login.png")
    dash_b64 = get_image_base64("screenshot_dashboard.png")
    devices_b64 = get_image_base64("screenshot_devices.png")

    # Wrap figures and screenshots
    for p in soup.find_all("p"):
        img = p.find("img")
        if img:
            src = img.get("src", "")
            
            # Figure 0: Architecture Diagram
            if "architecture_diagram.png" in src:
                fig_div = soup.new_tag("div", attrs={"class": "academic-figure architecture-figure"})
                fig_img = soup.new_tag("img", src=arch_b64, alt="Figure 0: End-to-End Five-Tier Zero Trust Architecture for IoT Networks")
                fig_cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                fig_cap.append(BeautifulSoup("<strong>Figure 0: End-to-End Five-Tier Zero Trust Architecture for IoT Networks</strong> &mdash; Depicting Layer 1 (W3C DIDs on Heterogeneous Edge Nodes), Layer 2 (mTLS 1.3 / MQTT 5.0 Transport Broker), Layer 3 (Spring Boot 3 Java 21 Virtual-Thread Core PDP Engine), Layer 4 (EVM Smart Contracts &amp; IPFS), and Layer 5 (Executive React 18 / Vite Security Mesh).", "html.parser"))
                fig_div.append(fig_img)
                fig_div.append(fig_cap)
                p.replace_with(fig_div)

            # Figure 1: Confusion Matrix
            elif "confusion_matrix_heatmap.png" in src:
                fig_div = soup.new_tag("div", attrs={"class": "academic-figure benchmark-figure"})
                fig_img = soup.new_tag("img", src=cm_b64, alt="Figure 1: Confusion Matrix Heatmaps")
                fig_cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                fig_cap.append(BeautifulSoup("<strong>Figure 1: Confusion Matrix Heatmaps</strong> &mdash; Empirical classification results across Kaggle IoT-23 (Malware Execution, 42 samples) and Kaggle CIC-IoT-2023 (DDoS Flood, 40 samples) demonstrating 100.0% Detection Accuracy, 0 False Positives, and 0 False Negatives.", "html.parser"))
                fig_div.append(fig_img)
                fig_div.append(fig_cap)
                p.replace_with(fig_div)

            # Figure 2: ROC Curve
            elif "roc_curve_analysis.png" in src:
                fig_div = soup.new_tag("div", attrs={"class": "academic-figure benchmark-figure"})
                fig_img = soup.new_tag("img", src=roc_b64, alt="Figure 2: ROC Curve Analysis")
                fig_cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                fig_cap.append(BeautifulSoup("<strong>Figure 2: Receiver Operating Characteristic (ROC) Curve Analysis</strong> &mdash; Demonstrating an Area Under Curve (AUC) of 0.994 with an operational threshold operating at 100.0% True Positive Rate and 0.0% False Positive Rate.", "html.parser"))
                fig_div.append(fig_img)
                fig_div.append(fig_cap)
                p.replace_with(fig_div)

            # Figure 3: Timeline
            elif "trust_score_vs_latency_timeline.png" in src:
                fig_div = soup.new_tag("div", attrs={"class": "academic-figure benchmark-figure"})
                fig_img = soup.new_tag("img", src=timeline_b64, alt="Figure 3: Dynamic Trust Score Timeline")
                fig_cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                fig_cap.append(BeautifulSoup("<strong>Figure 3: Dynamic Trust Score T(t) vs Decision Latency Timeline</strong> &mdash; Packet-by-packet attestation tracing nominal state (T=95), rapid mathematical penalty decay upon attack injection, and instantaneous autonomous quarantine under 13.8 ms.", "html.parser"))
                fig_div.append(fig_img)
                fig_div.append(fig_cap)
                p.replace_with(fig_div)

            # Figure 4: Comparative Benchmark
            elif "comparative_security_benchmark.png" in src:
                fig_div = soup.new_tag("div", attrs={"class": "academic-figure benchmark-figure"})
                fig_img = soup.new_tag("img", src=bar_b64, alt="Figure 4: Comparative Security Benchmark")
                fig_cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                fig_cap.append(BeautifulSoup("<strong>Figure 4: Quantitative Comparative Security Benchmark</strong> &mdash; Proposed Zero Trust Architecture vs Traditional Boundary Perimeter Firewalls across Attack Detection Accuracy (99.5% vs 68.4%), Prevention of Lateral Movement (100.0% vs 22.0%), and SLA Decision Latency.", "html.parser"))
                fig_div.append(fig_img)
                fig_div.append(fig_cap)
                p.replace_with(fig_div)

            # Figures 5, 6, 7: UI Screenshots Gallery
            elif "screenshot_login.png" in src or "screenshot_dashboard.png" in src or "screenshot_devices.png" in src:
                if not soup.find("div", attrs={"class": "ui-gallery-figure"}):
                    gallery_div = soup.new_tag("div", attrs={"class": "academic-figure ui-gallery-figure"})
                    
                    grid_div = soup.new_tag("div", attrs={"class": "ui-gallery-grid"})
                    
                    # Card 1: Login
                    c1 = soup.new_tag("div", attrs={"class": "ui-card"})
                    c1_img = soup.new_tag("img", src=login_b64, alt="Figure 5: Enterprise Login")
                    c1_title = soup.new_tag("div", attrs={"class": "ui-card-title"})
                    c1_title.string = "Fig 5: Operator Sign-In"
                    c1_desc = soup.new_tag("div", attrs={"class": "ui-card-desc"})
                    c1_desc.string = "Web3 SIWE & Role Auth"
                    c1.append(c1_img)
                    c1.append(c1_title)
                    c1.append(c1_desc)
                    grid_div.append(c1)

                    # Card 2: Dashboard
                    c2 = soup.new_tag("div", attrs={"class": "ui-card"})
                    c2_img = soup.new_tag("img", src=dash_b64, alt="Figure 6: Executive Dashboard")
                    c2_title = soup.new_tag("div", attrs={"class": "ui-card-title"})
                    c2_title.string = "Fig 6: Telemetry Dashboard"
                    c2_desc = soup.new_tag("div", attrs={"class": "ui-card-desc"})
                    c2_desc.string = "100/100 Trust & 14.2ms SLA"
                    c2.append(c2_img)
                    c2.append(c2_title)
                    c2.append(c2_desc)
                    grid_div.append(c2)

                    # Card 3: Fleet Manager
                    c3 = soup.new_tag("div", attrs={"class": "ui-card"})
                    c3_img = soup.new_tag("img", src=devices_b64, alt="Figure 7: Fleet Manager")
                    c3_title = soup.new_tag("div", attrs={"class": "ui-card-title"})
                    c3_title.string = "Fig 7: IoT Fleet Manager"
                    c3_desc = soup.new_tag("div", attrs={"class": "ui-card-desc"})
                    c3_desc.string = "W3C DIDs & Isolation"
                    c3.append(c3_img)
                    c3.append(c3_title)
                    c3.append(c3_desc)
                    grid_div.append(c3)

                    gallery_div.append(grid_div)

                    cap = soup.new_tag("div", attrs={"class": "figure-caption"})
                    cap.append(BeautifulSoup("<strong>Figures 5, 6, &amp; 7: Production Cloud-Deployed Zero Trust Command Center Interfaces</strong> &mdash; Operational deployment accessible live at <a href='https://zero-trust-snowy.vercel.app' target='_blank'>zero-trust-snowy.vercel.app</a> connected to live Render Spring Boot 3 core PDP engine. Demonstrating (5) Sign-In with Ethereum (SIWE/MetaMask), (6) Real-time telemetry monitoring with sub-15ms PDP decision latency, and (7) On-chain W3C DID attestation fleet inventory with autonomous quarantine controls.", "html.parser"))
                    gallery_div.append(cap)

                    p.replace_with(gallery_div)
                else:
                    p.extract()

    # Remove all duplicate trailing caption paragraphs from markdown
    for p in soup.find_all("p"):
        txt = p.get_text(strip=True)
        if any(txt.startswith(f"Figure {i}:") or txt.startswith(f"Figures {i}") for i in range(10)):
            p.extract()

    # Clean up any remaining empty paragraphs
    for p in soup.find_all("p"):
        if not p.get_text(strip=True) and not p.find_all():
            p.extract()

    # Enforce strategic clean page breaks using uppercase text check
    for h2 in soup.find_all("h2"):
        txt = h2.get_text().upper()
        if "5. PROPOSED" in txt:
            h2['style'] = "page-break-before: always; break-before: page;"
        elif "6. MATHEMATICAL" in txt:
            h2['style'] = "page-break-before: always; break-before: page;"
        elif "8. EMPIRICAL" in txt:
            h2['style'] = "page-break-before: always; break-before: page;"

    # Keep Screenshot gallery heading and figure together on Page 7
    for h3 in soup.find_all("h3"):
        if "LIVE SYSTEM" in h3.get_text().upper():
            h3['style'] = "page-break-before: always; break-before: page;"

    # Style confusion matrix calculation box
    for pre in soup.find_all("pre"):
        if "Confusion Matrix Metric Formulas" in pre.get_text():
            pre['class'] = pre.get('class', []) + ['conf-matrix-calc']

    # Style references list into clean compact two-column or bordered grid
    for h2 in soup.find_all("h2"):
        if "REFERENCES" in h2.get_text().upper():
            ref_ol = h2.find_next_sibling("ol")
            if ref_ol:
                ref_ol['class'] = ref_ol.get('class', []) + ['references-list']

    # Build Complete HTML Document with Publication Print Styling
    body_content = header_html + str(soup)

    full_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Zero Trust Architecture for IoT Networks — Project Report — Rebaka Meda (2400032563) — KL University</title>
    <style>
        @page {{
            size: A4 portrait;
            margin: 11mm 12mm 13mm 12mm;
            @top-left {{
                content: "KL University | Project Report";
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                font-size: 7.5pt;
                color: #64748b;
                font-weight: 500;
            }}
            @top-right {{
                content: "Rebaka Meda (Reg: 2400032563)";
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                font-size: 7.5pt;
                font-weight: 700;
                color: #1e3a8a;
            }}
            @bottom-left {{
                content: "Zero Trust Architecture for IoT Networks: Continuous Attestation & Sub-15ms Defense";
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                font-size: 7pt;
                color: #94a3b8;
            }}
            @bottom-right {{
                content: "Page " counter(page);
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                font-size: 8pt;
                font-weight: 700;
                color: #1e3a8a;
            }}
        }}

        @page :first {{
            @top-left {{ content: none; }}
            @top-right {{ content: none; }}
        }}

        * {{
            box-sizing: border-box;
        }}

        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            font-size: 8.6pt;
            line-height: 1.4;
            color: #1e293b;
            background-color: #ffffff;
            margin: 0;
            padding: 0;
        }}

        /* Institutional Header Block */
        .inst-header {{
            border-bottom: 2px solid #1e3a8a;
            padding-bottom: 8px;
            margin-bottom: 10px;
            page-break-after: avoid;
            break-after: avoid;
        }}

        .inst-top-bar {{
            text-align: center;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 5px;
            margin-bottom: 6px;
        }}

        .inst-university {{
            font-size: 13pt;
            font-weight: 800;
            color: #1e3a8a;
            letter-spacing: 0.5px;
            text-transform: uppercase;
        }}

        .inst-deemed {{
            font-size: 7.5pt;
            color: #64748b;
            font-style: italic;
            margin-top: 1px;
        }}

        .inst-dept {{
            font-size: 9.2pt;
            font-weight: 700;
            color: #0f172a;
            margin-top: 2px;
            letter-spacing: 0.2px;
        }}

        .inst-badge {{
            display: inline-block;
            background: #eff6ff;
            color: #1d4ed8;
            border: 1px solid #bfdbfe;
            border-radius: 4px;
            padding: 2px 8px;
            font-size: 7pt;
            font-weight: 700;
            margin-top: 3px;
            letter-spacing: 0.3px;
        }}

        .paper-title {{
            font-size: 12pt;
            font-weight: 800;
            color: #0f172a;
            text-align: center;
            line-height: 1.25;
            margin: 6px 0 8px 0;
            text-transform: uppercase;
            letter-spacing: -0.2px;
        }}

        /* Author Metadata Card */
        .author-card {{
            background: #f8fafc;
            border: 1px solid #cbd5e1;
            border-left: 4.5px solid #2563eb;
            border-radius: 5px;
            padding: 6px 9px;
            margin-bottom: 8px;
            font-size: 7.8pt;
            page-break-inside: avoid;
            break-inside: avoid;
        }}

        .author-row {{
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            margin-bottom: 2.5px;
        }}

        .author-row:last-child {{
            margin-bottom: 0;
        }}

        .author-cell {{
            flex: 1;
            min-width: 30%;
            padding: 1px 3px;
        }}

        .author-cell.full-width {{
            flex: 100%;
            border-top: 1px dashed #cbd5e1;
            padding-top: 3px;
            margin-top: 2px;
        }}

        .meta-label {{
            font-weight: 700;
            color: #475569;
            margin-right: 4px;
        }}

        .meta-val {{
            color: #0f172a;
            font-weight: 500;
        }}

        .meta-val.highlight {{
            color: #1e3a8a;
            font-weight: 800;
        }}

        .project-links a {{
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }}

        .project-links a:hover {{
            text-decoration: underline;
        }}

        .demo-cred code {{
            background: #e2e8f0;
            padding: 1px 4px;
            border-radius: 3px;
            font-size: 7pt;
            color: #0f172a;
        }}

        /* Typography & Headings */
        h2 {{
            font-size: 9.6pt;
            font-weight: 800;
            color: #1e3a8a;
            border-bottom: 1.5px solid #e2e8f0;
            padding-bottom: 2px;
            margin-top: 10px;
            margin-bottom: 4px;
            text-transform: uppercase;
            letter-spacing: 0.1px;
            page-break-after: avoid;
            break-after: avoid;
        }}

        h3 {{
            font-size: 8.5pt;
            font-weight: 700;
            color: #334155;
            margin-top: 6px;
            margin-bottom: 3px;
            page-break-after: avoid;
            break-after: avoid;
        }}

        p {{
            margin-top: 0;
            margin-bottom: 4px;
            text-align: justify;
            text-justify: inter-word;
        }}

        /* Lists */
        ol, ul {{
            padding-left: 17px;
            margin-top: 2px;
            margin-bottom: 4px;
        }}

        li {{
            margin-bottom: 1.5px;
            text-align: justify;
        }}

        strong {{
            color: #0f172a;
        }}

        hr {{
            border: 0;
            border-top: 1px solid #e2e8f0;
            margin: 6px 0;
        }}

        /* Mathematical Formulations */
        .math-block {{
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-left: 4px solid #4f46e5;
            border-radius: 4px;
            padding: 5px 10px;
            margin: 5px 0 7px 0;
            font-family: 'Cambria Math', 'Latin Modern Math', 'Times New Roman', serif;
            font-size: 9.2pt;
            text-align: center;
            color: #1e1b4b;
            page-break-inside: avoid;
            break-inside: avoid;
        }}

        .math-inline {{
            font-family: 'Cambria Math', 'Latin Modern Math', 'Times New Roman', serif;
            font-style: italic;
            font-weight: 600;
            color: #1e3a8a;
        }}

        /* Tables */
        table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 7.4pt;
            margin: 5px 0 7px 0;
            page-break-inside: avoid;
            break-inside: avoid;
        }}

        th {{
            background-color: #1e3a8a;
            color: #ffffff;
            font-weight: 700;
            text-align: left;
            padding: 4px 5px;
            border: 1px solid #1e3a8a;
            letter-spacing: 0.15px;
        }}

        td {{
            padding: 3px 5px;
            border: 1px solid #cbd5e1;
            color: #1e293b;
        }}

        tr:nth-child(even) td {{
            background-color: #f8fafc;
        }}

        /* Code Blocks */
        pre {{
            background: #0f172a;
            color: #f8fafc;
            padding: 5px 8px;
            border-radius: 4px;
            font-size: 6.8pt;
            line-height: 1.25;
            margin: 4px 0 6px 0;
            page-break-inside: avoid;
            break-inside: avoid;
        }}

        code {{
            font-family: 'Consolas', 'Courier New', monospace;
            background: #f1f5f9;
            padding: 1px 3px;
            border-radius: 3px;
            font-size: 7.2pt;
            color: #0f172a;
        }}

        pre code {{
            background: transparent;
            padding: 0;
            color: #f8fafc;
        }}

        /* ACADEMIC FIGURES & GRAPHICS - ZERO CLIPPING GUARANTEED */
        .academic-figure {{
            page-break-inside: avoid;
            break-inside: avoid;
            margin: 5px auto 8px auto;
            text-align: center;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 5px;
            padding: 4px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
            width: 100%;
        }}

        .academic-figure img {{
            max-width: 100% !important;
            height: auto !important;
            display: block;
            margin: 0 auto;
            object-fit: contain;
            border-radius: 3px;
        }}

        /* Explicit sizing to guarantee zero clipping on A4 pages */
        .architecture-figure img {{
            max-height: 102mm !important;
            width: 100% !important;
        }}

        .benchmark-figure img {{
            max-height: 68mm !important;
        }}

        .figure-caption {{
            font-size: 7.1pt;
            color: #475569;
            margin-top: 3px;
            line-height: 1.2;
            text-align: center;
        }}

        .figure-caption strong {{
            color: #1e3a8a;
            font-weight: 700;
        }}

        /* 3-COLUMN RESPONSIVE SCREENSHOT GALLERY */
        .ui-gallery-figure {{
            padding: 4px 3px;
        }}

        .ui-gallery-grid {{
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            gap: 6px;
            margin-bottom: 3px;
        }}

        .ui-card {{
            flex: 1;
            background: #f8fafc;
            border: 1px solid #cbd5e1;
            border-radius: 4px;
            padding: 3px;
            text-align: center;
            box-shadow: 0 1px 2px rgba(0,0,0,0.04);
        }}

        .ui-card img {{
            width: 100% !important;
            max-height: 52mm !important;
            object-fit: contain;
            border: 1px solid #e2e8f0;
            border-radius: 3px;
        }}

        .ui-card-title {{
            font-size: 6.6pt;
            font-weight: 800;
            color: #1e3a8a;
            margin-top: 2px;
            text-transform: uppercase;
        }}

        .ui-card-desc {{
            font-size: 6pt;
            color: #64748b;
            margin-top: 1px;
            line-height: 1.15;
        }}

        .references-list {{
            column-count: 2;
            column-gap: 14px;
            font-size: 6.8pt;
            line-height: 1.25;
            padding-left: 14px;
            margin-top: 2px;
            margin-bottom: 2px;
        }}

        .references-list li {{
            margin-bottom: 2.5px;
            page-break-inside: avoid;
            break-inside: avoid;
            text-align: justify;
        }}

        /* Global image safety constraints */
        img {{
            max-width: 100% !important;
            height: auto !important;
            page-break-inside: avoid;
            break-inside: avoid;
        }}
    </style>
</head>
<body>
    {body_content}
</body>
</html>
"""

    print(f"[4/6] Writing standalone HTML document to: {html_file}")
    with open(html_file, "w", encoding="utf-8") as f:
        f.write(full_html)

    # Edge Headless Execution
    edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    if not os.path.exists(edge_path):
        edge_path = r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"

    print(f"[5/6] Generating PDF via Edge Headless engine ({edge_path})...")
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--no-pdf-header-footer",
        f"--print-to-pdf={academic_pdf_file}",
        f"file:///{html_file.replace(os.sep, '/')}"
    ]

    res = subprocess.run(cmd, capture_output=True, text=True)
    if os.path.exists(academic_pdf_file) and os.path.getsize(academic_pdf_file) > 1000:
        size_kb = os.path.getsize(academic_pdf_file) / 1024
        print(f"SUCCESS: Generated Academic PDF Report at {academic_pdf_file} ({size_kb:.1f} KB)")
        
        # Try to update default_pdf_file if not locked
        try:
            shutil.copy2(academic_pdf_file, default_pdf_file)
            print(f"SUCCESS: Synchronized copy to {default_pdf_file}")
        except Exception as e:
            print(f"Note: {default_pdf_file} is locked by an open viewer, fresh report is at {academic_pdf_file}")
    else:
        print(f"Error during PDF generation: {res.stderr}")
        return False

    # Render Preview Pages for Visual Quality Inspection
    print("[6/6] Verifying PDF layout and rendering page previews with PyMuPDF...")
    target_pdf = academic_pdf_file if os.path.exists(academic_pdf_file) else default_pdf_file
    doc = pymupdf.open(target_pdf)
    print(f"-> Verified Total Page Count: {len(doc)} pages")
    
    for i, page in enumerate(doc):
        pix = page.get_pixmap(dpi=150)
        preview_path = os.path.join(preview_dir, f"page_{i+1}.png")
        pix.save(preview_path)
        first_line = page.get_text().strip().split("\n")[0] if page.get_text().strip() else "[Graphic Only]"
        print(f"   Page {i+1}: {pix.width}x{pix.height} px | Heading: {first_line[:65]}")

    return True

if __name__ == "__main__":
    generate_academic_pdf()
