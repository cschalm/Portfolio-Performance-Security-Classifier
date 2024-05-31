# Portfolio-Performance-Security-Classifier

[English below](#english)

## Inhalt

- [Motivation](#motivation)
- [Ziel](#ziel)
- [Probleme](#probleme)
- [Beispiele](#beispiele)
  - [Regionen](#regionen)
  - [Branchen](#branchen)
  - [Unternehmensgewichtung](#unternehmensgewichtung)
- [Verwendung](#verwendung)
  - [Vorbereitung](#vorbereitung)
  - [Ausführung](#ausführung)
  - [Nachbereitung](#nachbereitung)
- [Sourcecode](#sourcecode)

Dieses Tool ist eine Erweiterung der genialen Arbeit am 
[Portfolio-Performance-ETF-Import](https://github.com/Wafffelmonster/Portfolio-Performance-ETF-Import).

## Motivation

[Portfolio Performance](https://www.portfolio-performance.info/) ist das vielleicht 
beste kostenlose Tool, um Portfolios zu tracken. Besonders die Klassifizierungen 
von Vermögensgegenständen und die Erstellung von Auswertung sind sehr mächtig.  
Mir fehlt nur ein Datenabruf, um Metadaten zur Klassifizierung z.B. Land des 
Unternehmenssitzes, Brancheneinteilung nach 
[GICS](https://de.wikipedia.org/wiki/Global_Industry_Classification_Standard) 
automatisch ergänzt zu bekommen.  
Bei ETFs oder Fonds sind oft mehrere Länder oder Regionen und auch Branchen vertreten, 
die der jeweilige Index vorgibt.
Zusätzlich sind in ETFs und Fonds viele Unternehmen enthalten, so dass die genaue 
Gewichtung einzelner Unternehmen im gesamten Portfolio nicht erkennbar ist.  
Zu guter Letzt ist diese Aufteilung nicht fest, sondern die Indices werden regelmäßig - 
meist quartalsweise - angepasst, was dann auch zeitnah in den entsprechenden ETFs 
nachgebildet wird.  
Beispielsweise ist Russland aus den Indices für Schwellenländern entfernt worden, 
Nvidia steigt im Börsenwert und damit auch anteilig in den einschlägigen Indices und 
die Top 10 ändert sich in der Gewichtung und damit Reihenfolge abhängig von der Marktlage
bzw. Marktkapitalisierung.  
Ein manuelles Pflegen der Bestandteile eines Fonds oder ETFs ist damit sehr aufwändig und
regelmäßig nötig.

## Ziel

Glücklicherweise gibt es im Internet frei verfügbar Informationen zu den genannten Metadaten, 
wenn auch nicht alles über einen Anbieter und in ausreichender Qualität. Aber für ETFs gibt 
es Informationen zu den enthaltenen Anteilen nach Regionen und Branchen sowie den Top 10, also 
den 10 Wertpapieren mit den größten Anteilen im ETF.  
Bei Einzelaktien müssen ebenfalls Daten zur Region und Branche abgerufen werden, um alle 
Wertpapiere richtig Gruppieren zu können.  
Gerade bei ETFS ändern sich die Metadaten häufiger, eine Aktualisierung der Anteile muss von 
diesem Tool geleistet werden, soll es einen dauerhaften Mehrwert liefern.

## Probleme

Das automatische Klassifizieren von Aktien und ETFs oder Fonds stellt einige Herausforderungen:
* Bestimmung der Wertpapierart (Aktie oder Fond) anhand der ISIN
* Uneinheitlicher Datenabruf im Internet je nach Wertpapierart
* Unterschiedliche Schreibweisen für Aktien in mehreren ETFs, die als gleich erkannt und 
  behandelt werden müssen
  * Meta Platforms (ehem. Facebook), META PLATFORMS INC-CLASS A und Meta Platforms Inc. -> 
    Meta Platforms Inc.
  * Alphabet A (Google), Alphabet C (Google), ALPHABET INC CL A, ALPHABET INC CL C und Alphabet 
    Inc. -> Alphabet Inc.
* Das Wiedererkennen von erfolgten Klassifizierungen von Wertpapieren für das Update von Werten
* Das Erkennen von Updates im Portfolio, neue oder komplett verkaufte Wertpapiere müssen angepasst
  werden

## Beispiele

### Regionen

Das Gerüst legt man direkt in Portfolio Performance als Klassifizierung "Regionen" an. Dieses Tool 
sortiert Einzelaktien ein und verteilt die Anteile von ETFs.

![Regionen](src/site/examples/Beispiel%20Regionen.jpg "Beispiel Regionen")

### Branchen

Das Gerüst legt man direkt in Portfolio Performance als Klassifizierung "Branchen (GICS)" an. Dieses 
Tool sortiert Einzelaktien ein und verteilt die Anteile von ETFs.

![Branchen-Übersicht](src/site/examples/Beispiel%20Branchen%20(GICS)%20-%20Übersicht.jpg "Beispiel Branchen Übersicht")

Der Drill-Down durch Klick auf eine Branche könnte beispielhaft so aussehen.

![Branchen-Informationstechnologie](src/site/examples/Beispiel%20Branchen%20(GICS)%20-%20Informationstechnologie.jpg "Beispiel Branchen Informationstechnologie")

### Unternehmensgewichtung

Für diese Klassifizierung legt man in Portfolio Performance eine neue Klassifizierung namens 
"Unternehmensgewichtung" an. Dieses Tool versucht dann über ähnliche Namen von Wertpapieren 
oder Anteilen in ETFs eine Gruppierung, um so den tatsächlichen Portfolioanteil jeden Unternehmens 
direkt oder indirekt zu bestimmen. So lassen sich Klumpenrisiken durch Überschneidungen in ETFs 
im Portfolio erkennen.

![Unternehmensgewichtung-Übersicht](src/site/examples/Beispiel%20Unternehmensgewichtung%20-%20Übersicht.jpg "Beispiel Unternehmensgewichtung Übersicht")

Der Drill-Down durch Klick auf ein Unternehmen könnte beispielhaft so aussehen.

![Unternehmensgewichtung-Alphabet](src/site/examples/Beispiel%20Unternehmensgewichtung%20-%20Alphabet.jpg "Beispiel Unternehmensgewichtung Alphabet")

## Verwendung

### Vorbereitung

Dieses Tool ist in der Programmiersprache Java entwickelt. Zur Ausführung ist daher eine Java Runtime 
der Version 11 oder neuer nötig. Diese kann für gängige **Desktop** Betriebssysteme beim Hersteller Oracle 
heruntergeladen werden: [Download Java](https://www.oracle.com/java/technologies/?er=221886)

Das ausführbare Programm befindet sich [hier](dist/PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar).
Es muss auf den lokalen Rechner heruntergeladen werden. Dann wird noch die Datendatei von Portfolio 
Performance benötigt. Je nach gewünschter automatischer Klassifizierung sind vorher die entsprechenden 
Klassifizierungen in Portfolio Performance anzulegen, also
- Regionen
- Branchen (GICS)
- Unternehmensgewichtung

**WICHTIG**: Das Tool arbeitet auf dem XML-Format von Portfolio Performance! Falls die Datendatei in einem 
anderen Format gespeichert sein sollte (Binär oder passwortgeschützt), muss sie erst im XML-Format neu 
gespeichert werden!

### Ausführung

Sobald Java installiert und dieses Tool heruntergeladen ist, kann es auf der Kommandozeile gestartet werden:

> java -jar PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar -inputfile Portfolio.xml -outputfile Portfolio-NEU.xml

**WICHTIG**: inputfile und outputfile müssen sich unterscheiden, sonst droht im Fehlerfall Datenverlust!

Hilfe zu weiteren (optionalen) Parametern können mit folgendem Kommando aufgerufen werden:

> java -jar PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar -help

### Nachbereitung

Wenn die Ausführung erfolgreich war, kann die neue Datendatei in Portfolio Performance wie gewohnt geöffnet 
werden. Die neuen Klassifizierungen enthalten neu die eingruppierten Wertpapiere des Portfolios. Wie üblich 
kann nun die Sortierung geändert, die Farben neu gewählt oder Umgruppierungen von Wertpapieren vorgenommen 
werden. Auch kann es nötig sein, die Gruppennamen von Wertpapieren zu ändern, da die Schreibweise manchmal 
in den sie enthaltenen ETFs "unschön" ist, z.B. komplett in Großbuchstaben.

## Sourcecode

Der Quelltext für dieses Tool ist öffentlich auf 
[GitHub](https://github.com/cschalm/Portfolio-Performance-Security-Classifier) verfügbar. Mit installiertem
[Java](https://www.oracle.com/java/technologies/?er=221886) und [Maven](https://maven.apache.org/) als 
Build-Tool lässt es sich einfach mit allen benötigten Bibliotheken übersetzen. Auch ein Import in beliebige
IDE sollte einfach möglich sein.

# English

## Table of Contents

- [Motivation](#motivation)
- [Goal](#goal)
- [Problems](#problems)
- [Examples](#examples)
    - [Regions](#regions)
    - [Industries](#industries)
    - [Wompany Weighting](#company-weighting)
- [Usage](#usage)
    - [Preparation](#preparation)
    - [Execution](#execution)
    - [Postprocessing](#postprocessing)
- [Source code](#sourcecode)

This tool is an extension of the ingenious work on the
[Portfolio Performance ETF Import](https://github.com/Wafffelmonster/Portfolio-Performance-ETF-Import).

## Motivation

[Portfolio Performance](https://www.portfolio-performance.info/) is perhaps the best free tool for tracking 
portfolios. Especially the classifications of assets and the creation of evaluations are very powerful.  
The only thing I'm missing is a data retrieval function to add metadata for classification, e.g. country of
company headquarters, industry classification according to
[GICS](https://de.wikipedia.org/wiki/Global_Industry_Classification_Standard)
to be added automatically.  
ETFs or funds often include several countries or regions and also sectors, specified by the respective index.
In addition, ETFs and funds contain many companies, so that the exact weighting of individual
weighting of individual companies in the overall portfolio is not recognizable.  
Last but not least, this allocation is not fixed, but the indices are regularly - usually quarterly - adjusted.
adjusted regularly - usually quarterly - which is then promptly replicated in the corresponding ETFs.  
For example, Russia has been removed from the indices for emerging markets,
Nvidia's market capitalization increases and thus also proportionally in the relevant indices and
the top 10 changes in weighting and therefore order depending on the market situation and market capitalization.
Manual maintenance of the components of a fund or ETF is therefore very time-consuming and regularly necessary.

## Goal

Fortunately, there is information on the metadata mentioned freely available on the Internet,
even if not all of it is available from one provider and in sufficient quality. But for ETFs there is
information on the shares contained by region and sector as well as the top 10, i.e. the 10 securities 
with the largest shares in the ETF.  
For individual shares, data on the region and sector must also be retrieved in order to group all securities 
correctly.
With ETFS in particular, the metadata changes more frequently and the shares must be updated by this 
tool if it is to provide lasting added value.

## Problems

The automatic classification of shares and ETFs or funds poses a number of challenges:
* Determining the type of security (share or fund) based on the ISIN
* Inconsistent data retrieval on the Internet depending on the type of security
* Different spellings for shares in several ETFs that must be recognized and treated as the same
    * Meta Platforms (formerly Facebook), META PLATFORMS INC-CLASS A and Meta Platforms Inc. -> Meta Platforms Inc.
    * Alphabet A (Google), Alphabet C (Google), ALPHABET INC CL A, ALPHABET INC CL C and Alphabet
      Inc. -> Alphabet Inc.
* Recognizing classifications of securities for the update of values
* Recognizing updates in the portfolio, new or completely sold securities must be adjusted.

## Examples

### Regions

The framework is created directly in Portfolio Performance as the “Regions” classification. This tool
sorts individual shares and distributes the shares of ETFs.

![Regions](src/site/examples/Beispiel%20Regionen.jpg "Example Regions")

### Industries

The framework is created directly in Portfolio Performance as the classification “Sectors (GICS)”. This
tool sorts individual stocks and distributes the shares of ETFs.

![Industry overview](src/site/examples/Beispiel%20Branchen%20(GICS)%20-%20Übersicht.jpg "Example Industry Overview")

The drill-down by clicking on an industry could look like this.

![Industry Information Technology](src/site/examples/Beispiel%20Branchen%20(GICS)%20-%20Informationstechnologie.jpg "Example Industry Information Technology")

### Company Weighting

For this classification, you create a new classification in Portfolio Performance called “Company weighting” in 
Portfolio Performance. This tool then tries to use similar names of securities or shares in ETFs in order to 
determine the actual portfolio share of each company directly or indirectly. This makes it possible to identify 
cluster risks due to overlaps in ETFs in the portfolio.

![Company Weighting overview](src/site/examples/Beispiel%20Unternehmensgewichtung%20-%20Übersicht.jpg "Example company weighting overview")

The drill-down by clicking on a company could look like this.

![Company Weighting alphabet](src/site/examples/Beispiel%20Unternehmensgewichtung%20-%20Alphabet.jpg "Example company weighting Alphabet")

## Usage

### Preparation

This tool is developed in the Java programming language. A Java Runtime version 11 or newer is therefore required. 
Java can be downloaded for common **desktop** operating systems from the manufacturer Oracle: 
[Download Java](https://www.oracle.com/java/technologies/?er=221886)

The executable program can be found [here](dist/PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar).
It must be downloaded to the local computer. Then the data file from Portfolio Performance is required. Depending 
on the desired automatic classification, the corresponding classifications must first be created in Portfolio 
Performance, i.e.
- Regionen
- Branchen (GICS)
- Unternehmensgewichtung

These classifications have to be in German language for now.  

**IMPORTANT**: The tool works on the XML format of Portfolio Performance! If the data file is saved in a different
format (binary or password-protected), it must first be saved again in XML format!

### Execution

As soon as Java is installed and this tool is downloaded, it can be started on the command line:

> java -jar PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar -inputfile Portfolio.xml -outputfile Portfolio-NEU.xml

**IMPORTANT**: inputfile and outputfile must be different, otherwise there is a risk of data loss in the event 
of an error!

Help on further (optional) parameters can be called up with the following command:

> java -jar PortfolioPerformanceSecurityClassifier-1.0-SNAPSHOT.jar -help

### Postprocessing

If the execution was successful, the new data file can be opened in Portfolio Performance as usual. The new 
classifications now contain the grouped securities of the portfolio. As usual the sorting can now be changed, 
the colors can be reselected or securities can be regrouped. It may also be necessary to change the group names 
of securities, as the spelling in the ETFs they contain occasionally is “unattractive”, e.g. completely in capital 
letters.

## Source code

The source code for this tool is publicly available on
[GitHub](https://github.com/cschalm/Portfolio-Performance-Security-Classifier)
With installed [Java](https://www.oracle.com/java/technologies/?er=221886) and [Maven](https://maven.apache.org/) as a build tool, it can be easily compiled with all the required 
libraries. An import into any IDE should also be possible.
