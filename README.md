# Bot for generating motivation letters using OpenAI and Telegram

This is a bot that asks a user for role description, their motivation and about the company's product, then generates a motivation letter based on that information using OpenAI's GPT-5 model.

The bot is built using Java, Spring Boot, Kafka, and Docker.

## Prerequisites
- Docker Desktop installed on your machine
- An OpenAI API key
- A Telegram bot token
- Basic knowledge of using terminal/command line
- Git installed on your machine

## Instruction

1. [download](https://www.docker.com/products/docker-desktop/) and install docker desktop
2. run these commands in your terminal to clone this repository (replace ~/path/to/your/folder with the folder where you want the project saved)

```
cd ~/path/to/your/folder
git clone https://github.com/yourusername/motivation-letter-bot.git
cd motivation-letter-bot
```
3. go to .env.example file, rename it to .env and fill in your api keys **(you can get them from https://platform.openai.com/api-keys for openai, and @BotFather on telegram for your bot token)**
4. adjust user- and system-prompts from src/main/resources according to your needs 
5. run this command to start docker container and the bot

```
docker-compose up -d
```
6. open telegram and search for your bot by its username, start a chat and type /start to begin

## Stopping or restarting the bot

To stop the bot, run this command in the terminal from the project folder:

```
cd ~/path/to/your/folder/motivation-letter-bot
docker-compose down
```

To restart the bot, run this command in the terminal from the project folder:

```
cd ~/path/to/your/folder/motivation-letter-bot
docker-compose up -d
```

## Troubleshooting

- If Docker fails to start containers, make sure **Docker Desktop is running**.
- To update credentials or prompts, edit `.env` or the prompt file, then restart the bot:
  ```
  cd ~/path/to/your/folder/motivation-letter-bot
  docker-compose up -d
  ```

## License

This project is licensed under the MIT License.