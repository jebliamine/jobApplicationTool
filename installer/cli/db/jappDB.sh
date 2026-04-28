#!/bin/bash

CONTAINER_NAME="japp-postgres"
IMAGE="postgres:16"
VOLUME_NAME="japp-postgres-data"
WAIT_SECONDS=60

DB_NAME="japp"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_PORT="5432"

log() {
  echo "[jappDB] $1"
}

show_logs_for_60s() {
  log "Showing PostgreSQL logs for ${WAIT_SECONDS} seconds..."

  END=$((SECONDS + WAIT_SECONDS))

  while [ $SECONDS -lt $END ]; do
    clear
    echo "=========================================="
    echo " PostgreSQL Container Logs ($CONTAINER_NAME)"
    echo "=========================================="
    docker logs $CONTAINER_NAME 2>&1 | tail -30
    echo ""
    echo "Remaining: $((END - SECONDS)) sec"
    echo "Press CTRL+C to exit log view"
    sleep 2
  done

  log "Finished log display."
}

wait_until_ready() {
  log "Waiting for PostgreSQL readiness..."

  END=$((SECONDS + WAIT_SECONDS))

  while [ $SECONDS -lt $END ]; do
    if docker logs $CONTAINER_NAME 2>&1 | grep -q "database system is ready to accept connections"; then
      log "PostgreSQL is ready."
      return 0
    fi
    sleep 2
  done

  log "Timeout reached. PostgreSQL may still be starting."
}

check_docker() {
  docker info > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "Docker is not running."
    exit 1
  fi
}

container_exists() {
  docker ps -a --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"
}

container_running() {
  docker ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"
}

create_container() {
  log "Creating PostgreSQL container..."

  docker run --name $CONTAINER_NAME \
    -e POSTGRES_DB=$DB_NAME \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_PASSWORD=$DB_PASSWORD \
    -p $DB_PORT:5432 \
    -v $VOLUME_NAME:/var/lib/postgresql/data \
    -d $IMAGE

  if [ $? -ne 0 ]; then
    log "Failed to create container."
    exit 1
  fi
}

case "$1" in

  up)
    check_docker

    if container_exists; then
      log "Container exists."

      if container_running; then
        log "Container already running."
      else
        log "Starting container..."
        docker start $CONTAINER_NAME
      fi
    else
      create_container
    fi

    wait_until_ready
    show_logs_for_60s
    ;;

  down)
    log "Stopping container..."
    docker stop $CONTAINER_NAME
    ;;

  restart)
    check_docker
    log "Restarting container..."
    docker restart $CONTAINER_NAME
    wait_until_ready
    show_logs_for_60s
    ;;

  logs)
    docker logs -f $CONTAINER_NAME
    ;;

  status)
    docker ps -a --filter "name=$CONTAINER_NAME"
    ;;

  delete)
    log "Deleting container..."
    docker rm -f $CONTAINER_NAME
    ;;

  shell)
    docker exec -it $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME
    ;;

  info)
    echo "Container : $CONTAINER_NAME"
    echo "Database  : $DB_NAME"
    echo "User      : $DB_USER"
    echo "Port      : $DB_PORT"
    echo "Volume    : $VOLUME_NAME"
    ;;

  *)
    echo "Usage:"
    echo "  jappDB up"
    echo "  jappDB down"
    echo "  jappDB restart"
    echo "  jappDB logs"
    echo "  jappDB status"
    echo "  jappDB delete"
    echo "  jappDB shell"
    echo "  jappDB info"
    ;;
esac