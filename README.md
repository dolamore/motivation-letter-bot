##Instruction##

1. [download](https://www.docker.com/products/docker-desktop/) and install docker desktop
2. create portainer volume using this command
   ```
   docker volume create portainer_data
   ```
3. run portainer container using this command
    ```
   docker run -d \
   -p 8000:8000 \
   -p 9000:9000 \
   --name portainer \
   --restart=always \
   -v /var/run/docker.sock:/var/run/docker.sock \
   -v portainer_data:/data \
   portainer/portainer-ce:latest
   ```
4. open your browser and go to http://localhost:9000
5. register your account there
6. got to **stack** (left-side)->add stack
7. name your stack (e.g. motivation-letter-bot)
8. choose build method: repository
9. go to @userinfobot in telegram, and get your user id from there
10. 
