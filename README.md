Applicazione android per eseguire test su un modello di TensorFlow, addestrato sul dataset KuHAR.
L'applicazione è minimale, contiene solamente un tasto che una volta cliccato iniziarà il campionamento dei valori dell'accelerometro e del giroscopio. Restituisce l'inferenza ogni due secondi (finché non si clicca nuovamente il bottone).
All'interno dell'implementazione è presente un filtro passa-basso per evitare mis-classificazioni.
