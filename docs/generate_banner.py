"""Rebuild docs/banner.png: pip install -r docs/banner-requirements.txt."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
W, H = 1280, 380
BG, INK, MUTED = '#F0EDE3', '#303A32', '#626D60'
canvas = Image.new('RGB', (W, H), BG)
draw = ImageDraw.Draw(canvas)
font_path = ROOT / 'app/src/main/res/font/barlow_condensed.ttf'
boxes = []

def reserve(name, box):
    x0,y0,x1,y1=box
    assert 0 <= x0 < x1 <= W and 0 <= y0 < y1 <= H, (name, box)
    for other, b in boxes:
        assert x1 <= b[0] or x0 >= b[2] or y1 <= b[1] or y0 >= b[3], (name, other)
    boxes.append((name, box))

def text(name, value, xy, size, limits, color=INK):
    font=ImageFont.truetype(str(font_path), size)
    box=draw.textbbox(xy, value, font=font, anchor='lt')
    assert box[0]>=limits[0] and box[1]>=limits[1] and box[2]<=limits[2] and box[3]<=limits[3], (name,box,limits)
    reserve(name, box)
    draw.text(xy, value, font=font, fill=color, anchor='lt')

def paste(name, img, xy, width):
    img=img.convert('RGBA')
    height=round(img.height*width/img.width)
    reserve(name, (*xy,xy[0]+width,xy[1]+height))
    img=img.resize((width,height),Image.Resampling.LANCZOS)
    canvas.paste(img,xy,img)

def svg(name, file, xy, width):
    paste(name,Image.open(file.with_suffix('.png')),xy,width)

paste('App icon',Image.open(ROOT/'artwork/kitchen-timer-icon/kitchen-timer-icon.preview.png'),(48,97),180)
text('Title','Kitchen Timer',(266,95),80,(260,50,920,180))
text('Tagline','Made to feel like hardware.',(268,194),32,(260,185,920,235))
text('Craft','A study in material, light and interaction.',(268,254),24,(260,245,920,290),MUTED)
reserve('Divider',(956,62,958,318));draw.line((957,62,957,318),fill='#C7CBBE',width=2)
text('Platform label','BUILT FOR',(1006,65),18,(1000,55,1220,95),MUTED)
svg('Android',ROOT/'docs/brand/android.svg',(1006,112),52)
text('Android name','Android™',(1080,124),26,(1070,105,1230,170))
svg('Kotlin',ROOT/'docs/brand/kotlin.svg',(1006,229),174)
reserve('Accent',(48,345,922,348));draw.rectangle((48,345,921,347),fill='#C74430')
canvas.save(ROOT/'docs/banner.png',optimize=True)
print('Banner generated; all text and image bounds checked.')
