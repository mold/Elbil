Info om patchar
===

amp_arp.pd
---
Använder `amp` för att ge kontinuerlig återkoppling.

När `amp` är positiv (batteriet laddas) hörs ett stigande arpeggio/crescendo i takt med att `amp` stiger.

Värdet på `amp` påverkar arpeggiots tempo, och aktiverar ett ytterligare, ljusare, arpeggio vid höga värden. När `amp` stiger hörs även ett glissando som stiger proportionerligt.

När `amp` är negativt spelas istället dissonanta ljud för att få förararen att köra bättre. Phasors och oscillatorer samverkar för att skapa en vibrerande och otrevlig ljudbild i takt med att `amp` blir lägre.

amp_reward.pd
---
Använder `amp` för att vid mer bestämda händelser ge återkoppling till användaren.

Efter en period med positiv `amp` (det vill säga när `amp` går under 0) spelas ett arpeggio upp, vars längd samt pitch- och tempo-ökning påverkas av hur mycket batteriet laddades under tiden det laddades.

När `amp` är positiv spelas även ett ljud upp som indikerar att batteriet laddas. Detta kan dock inaktiveras för att göra patchen mer "ikon"-baserad.

