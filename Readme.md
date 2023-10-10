# Political Speech Evaluation

This project is a solution for political speech evaluation. The project provides an endpoint `GET /evaluation?url=url1&url=url2`, fetches the csv files for given url(s) and calculate data for these questions:  

1. Which politician gave the most speeches in 2013?
2. Which politician gave the most speeches on the topic "Innere Sicherheit"?
3. Which politician used the fewest words?

While doing that, If a question cannot be answered or there is more than one person has the same amount (an ambiguous solution) for any of questions, It returns null.


## How to run
To execute application please use the command below:

`sbt run`

This will run the application on port `9000`. To call service `localhost:9000/evaluation?url={URL}` 
or with more than one url `localhost:9000/evaluation?url={URL_1}&url={URL_2}&...` should be entered.

There is also postman script ([PoliticalSpeeches.postman_collection.json](PoliticalSpeeches.postman_collection.json)) exist and can be found in the project directory.

## How to execute tests
To execute tests please use the command below:

`sbt test`

## Versions

Used versions for the project are: <br>

|       | Version |
|:------|:-------:|
| Sbt   |  1.9.6  |
| Scala | 2.13.12 |
| Play  | 2.8.20  |

