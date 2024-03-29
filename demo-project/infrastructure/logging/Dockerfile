FROM alpine:3.4
MAINTAINER TAGOMORI Satoshi <tagomoris@gmail.com>
LABEL Description="Fluentd docker image" Vendor="Fluent Organization" Version="1.1"

# Do not split this into multiple RUN!
# Docker creates a layer for every RUN-Statement
# therefore an 'apk delete build*' has no effect
RUN apk update; apk --no-cache add ca-certificates openssl gcc  make g++\
                   build-base \
                   ca-certificates \
                   ruby \
                   ruby-irb \
                   ruby-rdoc \
                   ruby-dev && \
    echo 'gem: --no-document' >> /etc/gemrc && \
    gem install oj && \
    gem install json && \
    gem install fluentd -v 0.12.31 && \
    gem install fluent-plugin-kubernetes_metadata_filter && \
    gem install fluent-plugin-logzio && \
    apk del build-base ruby-dev && \
    rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

RUN adduser -D -g '' -u 1000 -h /home/fluent fluent
RUN chown -R fluent:fluent /home/fluent

# for log storage (maybe shared with host)
RUN mkdir -p /fluentd/log
# configuration/plugins path (default: copied from .)
RUN mkdir -p /fluentd/etc /fluentd/plugins

RUN chown -R fluent:fluent /fluentd

#USER fluent
WORKDIR /home/fluent

# Tell ruby to install packages as user
RUN echo "gem: --user-install --no-document" >> ~/.gemrc
ENV PATH /home/fluent/.gem/ruby/2.3.0/bin:$PATH
ENV GEM_PATH /home/fluent/.gem/ruby/2.3.0:$GEM_PATH

#this has private data will mount as secret
#COPY target/k8s/fluent.conf /fluentd/etc/

ENV FLUENTD_OPT=""
ENV FLUENTD_CONF="fluentd.conf"

EXPOSE 24224 5140

CMD exec fluentd -c /fluentd/etc/$FLUENTD_CONF -p /fluentd/plugins $FLUENTD_OPT