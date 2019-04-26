FROM openjdk:8

# Android SDK

ENV ANDROID_HOME /opt/android-sdk-linux

# Download Android SDK into $ANDROID_HOME
# You can find URL to the current version at: https://developer.android.com/studio/index.html
# Or https://github.com/Homebrew/homebrew-cask/blob/master/Casks/android-sdk.rb

RUN mkdir -p ${ANDROID_HOME} && \
    cd ${ANDROID_HOME} && \
    wget -q https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip -O android_tools.zip && \
    unzip android_tools.zip && \
    rm android_tools.zip

ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools

# Accept Android SDK licenses && install other elements
# For full list; see sdkmanager --list --verbose
RUN yes | sdkmanager --licenses && \
    sdkmanager 'platform-tools' && \
    sdkmanager 'extras;google;m2repository' && \
    sdkmanager 'extras;android;m2repository'

# SDK Specific

RUN sdkmanager 'platforms;android-28' && \
    sdkmanager 'build-tools;28.0.3'

# Install Node.js

ENV NODEJS_VERSION=11.12.0 \
    PATH=$PATH:/opt/node/bin

WORKDIR "/opt/node"

RUN apt-get update && apt-get install -y curl git ca-certificates --no-install-recommends && \
    curl -sL https://nodejs.org/dist/v${NODEJS_VERSION}/node-v${NODEJS_VERSION}-linux-x64.tar.gz | tar xz --strip-components=1 && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

RUN mkdir -p /frost/

WORKDIR /frost/

COPY . /frost/

CMD ["./docker_build.sh"]