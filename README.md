# Measure CPU on the clients


## The problem in a nutshell

Our clients sometimes have high CPU usage. We don't know what causes this, and
we would like to collect analytical data, that can show us the reason.

The idea is to collect important events from the client, and by analyzing it,
show the possible causes. These important events are periodic CPU usage events,
file transfer events (start, end, progress), client app opened/closed, and
anything that could help us pinpoint the problem.

These events then ingested into an analytical engine, that tries to show use the
most likely cause. A very simple method is to show correlation between the
derivative of the CPU usage is high, and the occurrence of an event type. It
could be further narrowed down with grouping by client OS, client version, OS
version, or anything that distributes the outcome. This should take us much
closer to identifying the cause.


## Assumptions

* We have clients around the world, and we don't want them to have huge
  latencies.
* We already have a method for authenticating the clients (e.g. JWT)

### Not investigated

* GDPR (what data can we collect, how we must store them)

## Client

The client sends
the [events](api/src/main/scala/com/github/esgott/mcpu/api/ClientEvent.scala)
through a [REST API](api/src/main/scala/com/github/esgott/mcpu/api/Api.scala).
This API is provided in multiple regions, and the DNS routes the client to the
appropriate endpoint. The client also sends metadata about itself in
[HTTP headers](api/src/main/scala/com/github/esgott/mcpu/api/Headers.scala).

Sending the events is a two-step process. As the client might not be able to
reach the service (e.g. no internet connection, our service is down, not enough
CPU to send events), it first stores the events in local storage. We assume that
this has a high chance to succeed. The client has a separate service that checks
the events written to local storage, and knows the last event sent (if it
doesn't know it yet, it can query through the REST API), and sends the events
that haven't been sent yet.


## Server

The server has distributed event ingestion in multiple regions. The ingestion
just puts the events onto a message bus (e.g. Kafka), and the events on the
message bus are collected into one computing region (e.g. Kafka Mirrormaker).

The first step in the computing region is to deduplicate the events. Events can
get duplicated because of client retries, or by Kafka itself (at least once
guarantee). We consider the `(timestamp, eventType, clientId)` tuple unique, and
the deduplication can be handled with Bloom filters. From this point we can use
a message bus that can give us exactly once guarantee  (e.g. Kafka Transactions)
, so we don't have to worry about duplication.

The next step is to store the data in a storage solution, where the performance
characteristics match our expectations. As we probably need to query time series
of events, Prometheus could be a good fit, but it's hard to tell without
building of proof of concept and measuring our queries.

The final step is a tool that helps us analyze the data. In case of Prometheus,
Grafana is a straightforward choice. We can build graphs that can visualize the
relation between the events and the CPU usage.

The ingestion has to know the last event we have seen for a client in any
region. Fortunately, eventual consistency is enough in this case. Any cheap
database that can handle this multi-region situation with eventual consistency
is good enough (e.g. AWS Dynamo DB).


## Advantages of this design

* The API is designed in a CQRS fashion. This allows us to scala the writes (
  event ingestion) and the reads (data analyzation) differently, as these have
  quite different characteristics.
* The client and the server can make progress without the other
* The services are stateless, which makes restarting and scaling horizontally
  easier
* A partitioned message bus can balance the load nicely

## Architecture diagram


