# gruppo1-pw2

Membri:
- Luca Mimmo
- Fabio Porzio
- Tommaso Brivio
- Marco Corradini

Sistema di Gestione Visitatori

Panoramica

Questo progetto implementa un sistema di gestione visitatori basato sul web utilizzando Java, Maven e Quarkus. 
Il sistema consente alle aziende di registrare i visitatori, monitorare il numero di visitatori presenti in sede e 
assegnare badge in modo dinamico.

Requisiti

Il cliente richiede un'interfaccia in cui i visitatori possano inserire le proprie informazioni personali(es. nome, 
cognome) e i dettagli della visita (es. data, ora, durata). 
Questo sistema mira a:
- Fornire una panoramica del flusso di visitatori previsto.
- Generare e assegnare badge ai visitatori.
- Consentire al personale di sicurezza di monitorare la presenza dei visitatori in tempo reale.

Funzionalità

Gestione Dati Visitatori e Dipendenti:
- Numero finito di badge visitatori disponibili.
- I dipendenti sono registrati con informazioni personali e reparto di appartenenza.
- I visitatori sono registrati con informazioni personali e il riferimento al dipendente ospitante.

Gestione Visite
- Registrazione dettagli delle visite, inclusi data, ora di inizio, durata prevista e badge assegnato.
- Segnalazione della fine visita per rendere nuovamente disponibile il badge.
- Il personale di portineria può visualizzare l'elenco dei visitatori per una data selezionata, incluso lo stato della 
  visita (programmata, in corso o completata) e il badge assegnato.
- Il personale di portineria può assegnare badge ai visitatori in arrivo e segnare la fine di una visita.

Accesso per Reparto
- I dipendenti possono registrare un nuovo ospite.
- I dipendenti possono richiedere una visita con almeno un giorno di anticipo.
- I dipendenti possono annullare una richiesta di visita se non ancora iniziata.

Dettagli Tecnici

Tecnologie Utilizzate
- Java con Maven per la gestione del progetto.
- Quarkus come framework per lo sviluppo backend.
- Qute come motore di template per pagine web dinamiche.
- Memorizzazione su file CSV per la gestione dei dati persistenti.

Autenticazione & Autorizzazione

- Gli utenti devono autenticarsi per accedere al sistema.
- Supporto per accessi multipli contemporanei con lo stesso utente.
- Applicazione di criteri minimi di complessità per le password.
- Miglioramento Opzionale: Tracciamento dello sviluppo tramite GitHub Issues.

Struttura del Progetto

Il codice segue le convenzioni Java ed è organizzato in classi e package appropriati per garantire leggibilità e 
modularità.