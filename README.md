# redisson-utils
A couple of useful classes related to Redisson client for Redis

[![Java CI with Maven](https://github.com/javiercanillas/redisson-utils/actions/workflows/maven-build.yml/badge.svg)](https://github.com/javiercanillas/redisson-utils/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=javiercanillas_redisson-utils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=javiercanillas_redisson-utils)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=javiercanillas_redisson-utils&metric=coverage)](https://sonarcloud.io/summary/new_code?id=javiercanillas_redisson-utils)

## Classes

### RLockable
Useful class to deal with Redisson Locks, it copies Java `Optional` structure. For example:

This is the smallest way to do something when the lock was acquired. If it is not acquired, it will return something else. 
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } ).orElse("non-acquired-lock-result")
```

In case you need to do something to get the default value when lock couldn't be acquired, then you can use `orElseGet`:
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } ).orElseGet(() -> return "non-acquired-lock-result")
```

Furthermore, if you need to throw an exception, because you expected to acquired the lock, then you can use `orElseThrow`:
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } ).orElseThrow(myException::new)
```
*Note*: You can supply a method that will return the excpetion to be thrown.

#### Optionals of RLockable
If you want to setup a `waitTime`, useful when you don't want to wait forever to acquire the lock, then:
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } )
        .withTimeUnit(TimeUnit.SECONDS)
        .withWaitTime(30)
        .orElse("non-acquired-lock-result")
```
Since Redis let you setup also the `leaseTime`, you can put it like this:
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } )
        .withTimeUnit(TimeUnit.SECONDS)
        .withWaitTime(30)
        .withLeaseTime(240)
        .orElse("non-acquired-lock-result")
```
*Note*: `leaseTime` and `waitTime` shares the same `timeUnit`.

Also, if you want to supply your own exception when interrupted while waiting the lock, you can use `withInterruptedExceptionSupplier`:
```java
var result = RLockable.of(redissonClient.getLock("myLock"), () -> {
            // do something inside lock
            return "acquired-lock-result";
        } )
        .withTimeUnit(TimeUnit.SECONDS)
        .withWaitTime(30)
        .withInterruptedExceptionSupplier(e -> {
            log.error("Interrupted waiting for lock: myLock");
            return new CustomException(...);
        })        
        .orElse("non-acquired-lock-result")
```
For more examples, check the tests about the class.

## How to install
If you prefer to use maven central releases, you can find it [here](https://search.maven.org/artifact/io.github.javiercanillas/redisson-utils). Also, if you support [Jitpack.io](https://jitpack.io/) you can find it [here](https://jitpack.io/#javiercanillas/redisson-utils)

