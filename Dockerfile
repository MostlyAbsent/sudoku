FROM node:20-slim AS base
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable
COPY . /usr/src
WORKDIR /usr/src

RUN apt-get update && \
    apt-get install -y -q --allow-unauthenticated \
    git \
		rlwrap \
		curl \
		default-jre \
    sudo

RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh
RUN chmod +x linux-install.sh
RUN sudo ./linux-install.sh

RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile --shamefully-hoist
RUN pnpm shadow-cljs release app
RUN pnpm tailwindcss -i global.css -o resources/public/assets/css/output.css --minify

RUN clj -T:build uber

CMD [ "java", "-jar", "target/app-standalone.jar" ]
