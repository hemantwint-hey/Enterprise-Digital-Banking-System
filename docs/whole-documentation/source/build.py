"""
Builds every PDF in docs/whole-documentation from the HTML fragments in source/content.

Usage (from anywhere):
    python docs/whole-documentation/source/build.py            # build all
    python docs/whole-documentation/source/build.py 02 04      # build only docs whose file name starts with 02 / 04

Each content file starts with a metadata comment:
    <!-- title: Database Documentation | subtitle: ER diagram, schema ... | out: 02-Database-Documentation.pdf -->
The rest of the file is the document body (HTML; diagrams in <pre class="mermaid">).
A table of contents is generated from <h2> headings.
"""
import html
import pathlib
import re
import subprocess
import sys
import datetime

SRC = pathlib.Path(__file__).resolve().parent
OUT = SRC.parent
CONTENT = SRC / "content"
BUILD = SRC / "_build"
CHROME_CANDIDATES = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
]

VERSION = "1.0"
TODAY = datetime.date.today().strftime("%d %B %Y")


def find_chrome():
    for c in CHROME_CANDIDATES:
        if pathlib.Path(c).exists():
            return c
    sys.exit("Chrome/Edge not found - edit CHROME_CANDIDATES in build.py")


def slug(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def add_toc(body):
    toc = []

    def repl(m):
        attrs, inner = m.group(1), m.group(2)
        plain = re.sub(r"<[^>]+>", "", inner)
        anchor = slug(plain)
        toc.append((anchor, plain))
        return f'<h2{attrs} id="{anchor}">{inner}</h2>'

    body = re.sub(r"<h2([^>]*)>(.*?)</h2>", repl, body, flags=re.S)
    items = "\n".join(f'<li><a href="#{a}">{html.escape(t)}</a></li>' for a, t in toc)
    return f'<nav class="toc"><h3>Contents</h3><ol>{items}</ol></nav>', body


def build(path, chrome):
    raw = path.read_text(encoding="utf-8")
    meta_match = re.match(r"\s*<!--(.*?)-->", raw, flags=re.S)
    meta = {}
    for part in meta_match.group(1).split("|"):
        k, _, v = part.partition(":")
        meta[k.strip()] = v.strip()
    body = raw[meta_match.end():]
    toc, body = add_toc(body)
    show_toc = meta.get("toc", "yes") != "no"

    template = (SRC / "template.html").read_text(encoding="utf-8")
    page = (template
            .replace("{{TITLE}}", meta["title"])
            .replace("{{SUBTITLE}}", meta.get("subtitle", ""))
            .replace("{{DOCNO}}", path.stem.split("-")[0])
            .replace("{{VERSION}}", VERSION)
            .replace("{{DATE}}", TODAY)
            .replace("{{TOC}}", toc if show_toc else "")
            .replace("{{BODY}}", body))

    BUILD.mkdir(exist_ok=True)
    html_file = BUILD / (path.stem + ".html")
    html_file.write_text(page, encoding="utf-8")
    pdf_file = OUT / meta["out"]
    subprocess.run([
        chrome, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
        f"--user-data-dir={BUILD / 'chrome-profile'}",
        "--virtual-time-budget=20000", "--run-all-compositor-stages-before-draw",
        f"--print-to-pdf={pdf_file}", html_file.as_uri(),
    ], check=True, capture_output=True)
    print(f"built {pdf_file.name}")


def main():
    chrome = find_chrome()
    filters = sys.argv[1:]
    for path in sorted(CONTENT.glob("*.html")):
        if filters and not any(path.name.startswith(f) for f in filters):
            continue
        build(path, chrome)


if __name__ == "__main__":
    main()
