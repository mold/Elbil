Info om patchar
===

amp_arp.pd
---
Använder `amp` för att ge kontinuerlig återkoppling.

När `amp` är positiv (batteriet laddas) hörs ett stigande arpeggio/crescendo i takt med att `amp` stiger.

Värdet på `amp` påverkar arpeggiots tempo, och aktiverar ett ytterligare, ljusare, arpeggio vid höga värden. När `amp` stiger hörs även ett glissando som stiger proportionerligt.

När `amp` < 0 finns det två subpatchar att välja mellan, varav `pd amp_negative_smooth` används (2014-08-18):

- `pd amp_negative2`: När `amp` är negativt spelas istället dissonanta ljud för att få förararen att köra bättre. Phasors och oscillatorer samverkar för att skapa en vibrerande och otrevlig ljudbild i takt med att `amp` blir lägre.

- `pd amp_negative_smooth`: Fungerar som en inverterad version av den positiva arpeggion. En mindre harmonisk arpeggio stiger i takt med att `amp` sjunker. Vagt inspirerat av retro-futuristiska rymdskeppsljud.

amp_reward.pd
---
Använder `amp` för att vid mer bestämda händelser ge återkoppling till användaren.

Efter en period med positiv `amp` (det vill säga när `amp` går under 0) spelas ett arpeggio upp, vars längd samt pitch- och tempo-ökning påverkas av hur mycket batteriet laddades under tiden det laddades.

När `amp` är positiv spelas även ett ljud upp som indikerar att batteriet laddas. Detta kan dock inaktiveras för att göra patchen mer "ikon"-baserad.

spd-acc-amp_energy-consumption.pd
---
Använder `speed`, `acceleration` och `amp` för att ge återkoppling om den momentära energiförbrukningen.

Fungerar precis som `amp_reward.pd` men kopplar också ett low-pass-filter till `speed` vilket gör ljuden vassare vid högre hastigheter, samt använder `acceleration` för att endast ge feedback då körningen förändras (alltså när hastigheten inte är konstant).

amp_energy-success.pd
---
Använder `amp_high` och `amp_low`, baserade på `amp`, för att ge feedback när batteriet har "laddat" respektive förlorat energi.

`amp_high` ger utslag när amp har ökat med en viss hastighet under en viss period och triggar ett "positivt" ljud (`gain_1.wav`).

`amp_low` ger utslag när amp har minskat med en viss hastighet under en viss period och triggar ett "negativt" ljud (`loss_1.wav`).

amp_energy-success-amount.pd
---
Använder `amp_high`, `amp_low` och `amp_gain_time`, baserade på `amp`, för att ge feedback när batteriet har "laddat" respektive förlorat energi.

Fungerar som `wavfile.pd` men spelar också upp ett upp-pitchande ljud baserat på `amp_gain_time`.

template.pd
---
En template-patch som tar in några av de värden som sänds från PdBase: `soc`, `speed`, `fan`, `climate` och `amp`.
