import json
from pathlib import Path

import jieba
from rank_bm25 import BM25Okapi


DOCS = json.loads((Path(__file__).resolve().parents[1] / "runbooks.json").read_text(encoding="utf-8"))
INDEX = BM25Okapi([list(jieba.cut(doc["title"] + " " + doc["body"])) for doc in DOCS])


def search(query, component=None, version=None, limit=3):
    scores = INDEX.get_scores(list(jieba.cut(query)))
    ranked = sorted(enumerate(scores), key=lambda pair: pair[1], reverse=True)
    matches = []
    for index, score in ranked:
        doc = DOCS[index]
        if component and component not in doc["components"]:
            continue
        if version and doc["version"] not in (version, "all"):
            continue
        if score <= 0:
            continue
        matches.append({**doc, "score": round(float(score), 3)})
        if len(matches) == limit:
            break
    return matches
