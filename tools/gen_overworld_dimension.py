import json
import sys
import zipfile
from pathlib import Path

DIMENSION_PATH = "data/minecraft/dimension/overworld.json"
TARGET = "terralith:brushland"
LOW_EROSION_SPLIT = -0.375
HIGH_EROSION_SPLIT = 0.05
LOW_EROSION_BIOME = "terralith:arid_highlands"
HIGH_EROSION_BIOME = "minecraft:savanna"
HUMID_BAND = [0.1, 0.3]
HUMID_BIOME = "terralith:shrubland"
MIN_SPLIT_WIDTH = 0.5


def load_dimension(source):
    source = Path(source)
    if source.suffix == ".jar" or source.suffix == ".zip":
        with zipfile.ZipFile(source) as jar:
            return json.loads(jar.read(DIMENSION_PATH))
    if source.is_dir():
        return json.loads((source / DIMENSION_PATH).read_text())
    return json.loads(source.read_text())


def with_erosion(entry, lo, hi, biome):
    params = dict(entry["parameters"])
    params["erosion"] = [lo, hi]
    return {"parameters": params, "biome": biome}


def split_entry(entry):
    lo, hi = entry["parameters"]["erosion"]
    if hi - lo <= MIN_SPLIT_WIDTH:
        return [entry]
    cuts = [lo]
    if lo < LOW_EROSION_SPLIT < hi:
        cuts.append(LOW_EROSION_SPLIT)
    if lo < HIGH_EROSION_SPLIT < hi:
        cuts.append(HIGH_EROSION_SPLIT)
    cuts.append(hi)
    if len(cuts) == 2:
        return [entry]
    out = []
    for a, b in zip(cuts, cuts[1:]):
        if b <= LOW_EROSION_SPLIT:
            biome = LOW_EROSION_BIOME
        elif a >= HIGH_EROSION_SPLIT:
            biome = HIGH_EROSION_BIOME
        else:
            biome = TARGET
        out.append(with_erosion(entry, a, b, biome))
    return out


def transform(dimension):
    biomes = dimension["generator"]["biome_source"]["biomes"]
    result = []
    stats = {"repointed_humid": 0, "split": 0, "added": 0}
    for entry in biomes:
        if entry["biome"] != TARGET:
            result.append(entry)
            continue
        if entry["parameters"]["humidity"] == HUMID_BAND:
            result.append({"parameters": entry["parameters"], "biome": HUMID_BIOME})
            stats["repointed_humid"] += 1
            continue
        parts = split_entry(entry)
        if len(parts) > 1:
            stats["split"] += 1
            stats["added"] += len(parts) - 1
        result.extend(parts)
    dimension["generator"]["biome_source"]["biomes"] = result
    return dimension, stats


def main():
    if len(sys.argv) != 3:
        print("usage: python gen_overworld_dimension.py <terralith.jar|terralith_dir|overworld.json> <output overworld.json>")
        sys.exit(1)
    dimension, stats = transform(load_dimension(sys.argv[1]))
    out = Path(sys.argv[2])
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(dimension, indent=2))
    print(json.dumps(stats))


if __name__ == "__main__":
    main()
