#!/usr/bin/env python

from __future__ import print_function
import sys

FLAT = "Flat"
SMOOTH = "Smooth"
UNTEXTURED = "Untextured"
TEXTURED = "Textured"
ALWAYS = None

# INPUT: Model model, Triangle t, Vertex a, b, c, UVCoord uva, uvb, uvc, Material material, Vector lightVector, float lightIntensity, float lightAmbient
PER_TRIANGLE = [
    (ALWAYS, "float", "Kd", "lightIntensity * material.diffuseValue"),
    (ALWAYS, "float", "Ka", "lightAmbient * material.ambientValue"),
    (FLAT,   "float", "lightAmt", "Math.max(normal.dot(lightVector), 0) * (Kd - Ka) + Ka"),
    (UNTEXTURED, "int", "red", "(material.color >> 16) & 0xff"),
    (UNTEXTURED, "int", "green", "(material.color >> 8) & 0xff"),
    (UNTEXTURED, "int", "blue", "(material.color) & 0xff"),
    ([FLAT, UNTEXTURED], "int", "color", "(int) (blue * lightAmt) | (((int) (green * lightAmt)) << 8) | (((int) (red * lightAmt)) << 16)"),
    (TEXTURED, "Texture", "texture", "material.texture"),
    (TEXTURED, "float", "texXMax", "texture.width - 1"),
    (TEXTURED, "float", "texYMax", "texture.height - 1"),
]

# INPUT: Vertex vert, UVCoord uv, any value or input in PER_TRIANGLE
PER_VERTEX = [
    (SMOOTH, "float", "lightAmt", "Math.max(vert.n.dot(lightVector), 0) * (Kd - Ka) + Ka")
]
for a in ["x", "y", "z"]:
    t = "int" if a == "y" else "float"
    PER_VERTEX.append((ALWAYS, t, a, "{}vert.proj{}".format("({})".format(t) if t != "float" else "", a.upper())))
for a in ["u", "v"]:
    PER_VERTEX.append((TEXTURED, "float", "tex" + a, "uv." + a + " * vertz"))

# INPUT: any value in PER_TRIANGLE or PER_VERTEX, any input to PER_TRIANGLE
# OUTPUT: int color
PER_PIXEL = [
    ([SMOOTH, UNTEXTURED], "int", "color", "(int) (blue * lightAmt) | (((int) (green * lightAmt)) << 8) | (((int) (red * lightAmt)) << 16)"),
    (TEXTURED, "float", "recip", "1/z"),
    (TEXTURED, "int", "tex_index", "((int) (Util.clamp(texv * recip, 0f, 1f) * texYMax)) * texture.width + (int) (Util.clamp(texu * recip, 0f, 1f) * texXMax)"),
    (TEXTURED, "int", "base_color", "texture.pixels[tex_index]"),
    (TEXTURED, "int", "red", "(base_color >> 16) & 0xff"),
    (TEXTURED, "int", "green", "(base_color >> 8) & 0xff"),
    (TEXTURED, "int", "blue", "(base_color) & 0xff"),
    (TEXTURED, "int", "color", "(int) (blue * lightAmt) | (((int) (green * lightAmt)) << 8) | (((int) (red * lightAmt)) << 16)")
]

gen = ""
def write(s):
    global gen
    gen += s + "\n"

write("switch(material.mode) {")

def mode_matches(smooth, textured, filter):
    if filter == ALWAYS:
        return True
    if filter == FLAT:
        return not smooth;
    if filter == SMOOTH:
        return smooth;
    if filter == UNTEXTURED:
        return not textured;
    if filter == TEXTURED:
        return textured;
    return (SMOOTH if smooth else FLAT) in filter and (TEXTURED if textured else UNTEXTURED) in filter

for (mode, smooth, textured) in [("FLAT", False, False), ("SMOOTH", True, False), ("TEXTURED", False, True), ("SMOOTH_TEXTURED", True, True)]:

    pt = [(x[1], x[2], x[3]) for x in PER_TRIANGLE if mode_matches(smooth, textured, x[0])]
    pv = [(x[1], x[2], x[3]) for x in PER_VERTEX   if mode_matches(smooth, textured, x[0])]
    pp = [(x[1], x[2], x[3]) for x in PER_PIXEL    if mode_matches(smooth, textured, x[0])]

    write("        case Material.{}: {{".format(mode))

    for (t, v, e) in pt:
        write("        final {} {} = {};".format(t, v, e))
    for vertex in ["a", "b", "c"]:
        for (t, v, e) in pv:
            write("        final {} {}{} = {};".format(t, vertex, v, e.replace("vert", vertex).replace("uv", "uv" + vertex)))
    for (t, v, _) in pv:
        for vert1 in ["a", "b"]:
            for vert2 in ["b", "c"]:
                if vert1 < vert2 and v != "y":
                    write("        final {t} d{v}_{v1}{v2} = {e};".format(t=t, v1=vert1, v2=vert2, v=v, e="({v1}y == {v2}y) ? ({v2}{v}-{v1}{v}) : (({v1}{v}-{v2}{v}) / ({v1}y-{v2}y))".format(v1=vert1, v2=vert2, v=v)))

    write("        int y = Math.max(ay, 0);")
    write("        final int relativeStartY = y - ay;")
    for toRight in [True, False]:
        if toRight:
            write("        if (dx_ab > dx_ac) { // case 1: point b is right of line a-c")
        else:
            write("        } else { // case 2: point b is left of line a-c")
        for top in [True, False]:
            (start, end) = ("a", "b") if top else ("b", "c")
            write("            {new}yend = Math.min({}y, height);".format(end, new=("int " if top else "")))
            (start_counter, end_counter) = ("ac".format(v=v), "{start}{end}".format(v=v, start=start, end=end))
            if not toRight:
                (start_counter, end_counter) = (end_counter, start_counter)
            for (t, v, _) in pv:
                if v == "y":
                    continue
                if top:
                    write("            {t} s{v} = a{v} + relativeStartY * d{v}_{start_counter};".format(t=t, v=v, start_counter=start_counter))
                    write("            {t} e{v} = a{v} + relativeStartY * d{v}_{end_counter};".format(t=t, v=v, end_counter=end_counter))
                else:
                    write("            {se}{v} = b{v};".format(v=v, se=("e" if toRight else "s")))
            write("            for (; y < yend; ++y) {")
            for (t, v, _) in pv:
                if v == "y" or v == "x":
                    continue
                write("                {t} {v} = s{v};".format(t=t, v=v))
                write("                final {t} d{v} = (s{v} - e{v}) / (sx - ex);".format(t=t, v=v))
            write("                final int row = y * width;")
            write("                for (int index = row + (int)Math.max(sx, 0); index < row + (int)Math.min(ex, width - 1); ++index) {")
            write("                    if (zbuf[index] < z) {")
            for (t, v, e) in pp:
                write("                        final {t} {v} = {e};".format(t=t, v=v, e=e))
            write("                        zbuf[index] = z;")
            write("                        pixels[index] = color | ALPHA;")
            write("                        modelbuf[index] = model;")
            write("                    }")
            for (t, v, _) in pv:
                if v == "y" or v == "x":
                    continue
                write("                    {v} += d{v};".format(v=v))
            write("                }")
            for (t, v, _) in pv:
                if v == "y":
                    continue
                write("                s{v} += d{v}_{start_counter};".format(v=v, start_counter=start_counter))
                write("                e{v} += d{v}_{end_counter};".format(v=v, end_counter=end_counter))
            write("            }")
    write("        }")

    write("        }")
    write("        break;")

write("        }")

print(sys.stdin.read().replace("/* {AUTOGENERATED CODE HERE} */", gen))
