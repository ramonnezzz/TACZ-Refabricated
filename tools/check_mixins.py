#!/usr/bin/env python3
"""
Valida os alvos dos mixins do TACZ contra o jar (desobfuscado) do Minecraft 26.2.

Para cada @Mixin no source, resolve a classe-alvo (via @Mixin(...) + import), extrai
todos os `method = "..."` das injeções e confere, com `javap`, se cada método existe
na classe-alvo — comparando também o descriptor quando o mixin o especifica.

Uso:
    python3 tools/check_mixins.py /caminho/para/minecraft-26.2-client.jar

Se não passar o jar, tenta achar em ~/.gradle e nos launchers comuns (Prism/oficial).
"""
import glob
import os
import re
import subprocess
import sys

SRC = "src/main/java"


def find_jar(argv):
    if len(argv) > 1:
        return argv[1]
    patterns = [
        os.path.expanduser("~/.gradle/caches/**/minecraft-*-client*.jar"),
        os.path.expanduser("~/.gradle/caches/**/*minecraft*merged*.jar"),
        os.path.expanduser("~/snap/**/minecraft/26.2/minecraft-26.2-client.jar"),
        os.path.expanduser("~/.local/share/PrismLauncher/**/minecraft-26.2-client.jar"),
        os.path.expanduser("~/.minecraft/versions/26.2/26.2.jar"),
    ]
    for p in patterns:
        hits = glob.glob(p, recursive=True)
        if hits:
            return hits[0]
    return None


def resolve_target(txt):
    m = re.search(r'@Mixin\(\s*(?:value\s*=\s*)?\{?\s*([A-Za-z0-9_.$]+)\.class', txt)
    if not m:
        return None
    simple = m.group(1)
    if simple.startswith("net.minecraft") or "." in simple.split("$")[0] and simple.count(".") > 1:
        return simple
    outer = simple.split("$")[0]
    inner = simple[len(outer):]
    im = re.search(r'^import\s+(net\.minecraft\.[\w.]+\.' + re.escape(outer) + r');', txt, re.M)
    if im:
        return im.group(1) + inner.replace(".", "$")
    # nome simples estilo Outer.Inner sem import direto do inner
    m2 = re.match(r'([A-Za-z0-9_]+)\.([A-Za-z0-9_.$]+)', simple)
    if m2:
        im2 = re.search(r'^import\s+(net\.minecraft\.[\w.]+\.' + re.escape(m2.group(1)) + r');', txt, re.M)
        if im2:
            return im2.group(1) + "$" + m2.group(2).replace(".", "$")
    return simple


def extract_methods(txt):
    # pega todos os method = "..." de @Inject/@Redirect/@ModifyArg/@ModifyVariable/@ModifyExpressionValue/@WrapOperation
    return re.findall(r'method\s*=\s*"([^"]+)"', txt)


def javap_methods(jar, fqn):
    internal = fqn.replace(".", "/")  # javap aceita FQN com . ; inner com $
    try:
        out = subprocess.run(
            ["javap", "-p", "-s", "-classpath", jar, fqn],
            capture_output=True, text=True, timeout=60,
        ).stdout
    except Exception as e:
        return None, f"javap falhou: {e}"
    if not out.strip():
        return None, "classe não encontrada no jar"
    names = set()
    pairs = set()
    lines = out.splitlines()
    for i, ln in enumerate(lines):
        mm = re.search(r'\b([A-Za-z0-9_$<>]+)\(', ln)
        if mm and ("(" in ln) and (";" in ln or "{" in ln):
            name = mm.group(1)
            # construtores aparecem como o nome da classe; normaliza <init>
            if name == fqn.split(".")[-1].split("$")[-1]:
                name = "<init>"
            desc = None
            for j in range(i + 1, min(i + 3, len(lines))):
                dm = re.search(r'descriptor:\s*(\S+)', lines[j])
                if dm:
                    desc = dm.group(1)
                    break
            names.add(name)
            if desc:
                pairs.add((name, desc))
    return (names, pairs), None


def main():
    jar = find_jar(sys.argv)
    if not jar or not os.path.exists(jar):
        print("!! jar do Minecraft não encontrado. Passe o caminho:\n"
              "   python3 tools/check_mixins.py /caminho/minecraft-26.2-client.jar")
        sys.exit(1)
    print(f"# jar: {jar}\n")

    broken = []
    cache = {}
    for f in sorted(glob.glob(f"{SRC}/**/*Mixin*.java", recursive=True)):
        txt = open(f, encoding="utf-8", errors="ignore").read()
        if "@Mixin" not in txt:
            continue
        target = resolve_target(txt)
        methods = extract_methods(txt)
        if not target or not methods:
            continue
        if target not in cache:
            cache[target] = javap_methods(jar, target)
        info, err = cache[target]
        rel = os.path.relpath(f)
        if err:
            broken.append((rel, target, "-", err))
            continue
        names, pairs = info
        for spec in methods:
            mname = spec.split("(")[0]
            if mname in ("<init>",):
                continue
            if "(" in spec:
                desc = spec[spec.index("("):]
                ok = (mname, desc) in pairs or mname in names  # nome ok mas descriptor mudou => reporta
                if (mname, desc) not in pairs:
                    hint = "método existe, DESCRIPTOR mudou" if mname in names else "método NÃO existe"
                    broken.append((rel, target, spec, hint))
            else:
                if mname not in names:
                    broken.append((rel, target, spec, "método NÃO existe"))

    if not broken:
        print("OK — todos os alvos de método dos mixins existem no jar 26.2.")
        return
    print("ALVOS QUEBRADOS (arquivo | classe | method= | motivo):\n")
    for rel, target, spec, why in broken:
        print(f"- {rel}\n    classe : {target}\n    method : {spec}\n    motivo : {why}\n")
    print(f"total: {len(broken)} alvo(s) quebrado(s)")


if __name__ == "__main__":
    main()
