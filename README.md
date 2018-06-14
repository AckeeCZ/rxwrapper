# RxWrapper Android Library
This simple library provides mechanism to add custom .compose(Observable.Transformer) to every method, that returns Observable<T> via annotation processor and generated classes

## Purpose of this library
The main purpose of this library was usage of our RxOauth library that requires to add `.compose(rxManager.wrapWithOauthHandling())` code to every request. It`s boilerplate code, relying on developer awarness to at this code to every new request he writes.
 
## Dependencies
```groovy
apt"cz.ackee.rxwrapper:processor:x.x.x"
compile "cz.ackee.rxwrapper:annotations:x.x.x"
```

### Usage
The usage is simple, the best case scenario can be demonstrated with Retrofit ApiService description. Simply annotate this class with `@WrappedService` annotation and wrapping class will be generated

Example: Simple retrofit api service
```java
/**
 * Simple retrofit api description
 **/
@WrappedService
public interface ApiDescription {
 
    @NoCompose
    @POST("login")
    public Observable<LoginResponse> login(@Query("name") String name, @Query("password") String passwd);
    
    @GET("data")
    public Observable<List<SampleItem>> getData();
 
    @NoCompose
    @POST("refresh-access-token")
    Observable<ICredentialsModel> refreshAccessToken(@Query("refreshToken")String refreshToken);
}
```

and this class is generated: 
```java

/**
 * Generated class that encapsulates method of ApiDescription with RxWrapper handling
 */
public final class ApiDescriptionWrapped {
  ApiDescription service;
 
  IRxWrapper rxWrapper;
 
  public ApiDescriptionWrapped(ApiDescription service, IRxWrapper rxWrapper) {
    this.service = service;
    this.rxWrapper = rxWrapper;
  }
 
  public Observable<LoginResponse> login(String name, String passwd) {
    return this.service.login(name, passwd);
  }
 
  public Observable<List<SampleItem>> getData() {
    return this.service.getData().compose(this.rxWrapper.wrap());
  }
 
  public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
    return this.service.refreshAccessToken(refreshToken);
  }
}
```

Only methods that returns `Observable<T>`,`Single<T>` or `Completable<T>` are suffixed with wrapping. If you dont want to generate that compose thing, just annotate method with `@NoCompose` annotation as you can see in example.

You have to make at least one successful compilation to have access to generated Wrapper class. 

Usage of wrapper:
```java
RxOauthManager manager = ...
ApiDescription apiDescription = ...
ApiDescription apiWrapper = new ApiDescriptionWrapped(apiDescription, new IComposeWrapper() {
            @Override
            public <T> Observable.Transformer<T, T> wrap() {
                return rxOauthManaging.wrapWithOAuthHandling();
            }
        });
```

and then, wherever you would use original `ApiDescription` class you will use `apiWrapper`