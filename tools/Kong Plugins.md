<center> <h1> Kong Plugins  </h1> </center>



Kong is an API Gateway. It gives lot of plugins, which helps us to add features according to our needs.



Basically, Kong Plugins are divided in following categories :

1)  Inbuilt Plugins ( aka bundled ) - managed by Kong Inc.

2) Third-Party Plugins - managed by third party companies

   

We can configure kong via different ways :

1) Db config - Classic mode
2) Static config file - Dbless mode

Some Plugins support both modes.And some supports any of one mode.

 

Also, Kong comes with the following versions :

1) Kong Community Version 
2) Kong Enterprise Version

Kong Enterprise comes up with added support and some more useful plugins.



 <h6> Terminology : </h6>

- **Plugin**

  An extension to the Kong Gateway, written in Lua, Go, Python or Javascript.

- **Kong plugin**

  A plugin developed, maintained, and supported by Kong.

- **Third-party or Community plugin**

  A custom plugin developed, maintained, and supported by an external developer, not by Kong. Kong does not test these plugins, or update their version compatibility. 

- **Service**

  The Kong entity representing an external upstream API or microservice.

- **Route**

  The Kong entity representing a way to map downstream requests to upstream services.

- **Consumer**

  An entity that makes requests for Kong to proxy. It represents either a user or an external service.

- **Credential**

  A unique string associated with a Consumer; also referred to as an API key.

- **Upstream**

  The Kong entity that refers to your own API/service sitting behind Kong, to which client requests are forwarded.



Plugins are divided in following categories :

1) Authentication
2) Security
3) Traffic Control
4) Serverless
5) Analytics & Monitoring
6) Transformations
7) Logging
8) Deployment



Lets deep dive in some of the Authentication plugins :

<h4> JWT : </h4>

Verify requests containing HS256 or RS256 signed JSON Web Tokens.

Kong will either proxy the request to your Upstream services if the token’s signature is verified, or discard the request if not.

Kong can also perform verifications on some of the registered claims of RFC 7519 (exp and nbf).

It supports Dbless mode.

It is bundled with Kong Community Version.

Usage :

We can apply this plugin at following levels :

a) Service

b) Route

c) Globally - applied to all services



We can configure this plugin via :

1) Admin API
2) Kubernetes
3) Declarative (YAML)
4) Kong Manager



Lets configure JWT plugin via declarative (YAML) - 

```yaml
_format_version: "2.1"
services:
- name: my-api-server
  url: <service-url>
routes:
- name: my-api-request
  service: my-api-server
  paths:
    - /api
plugins:
- name: jwt
  enabled: true
  config:
    key_claim_name: iss
    claims_to_verify:
    - exp
consumers:
  - username: login_server_issuer
jwt_secrets:
  - consumer: login_server_issuer
    secret: "e71829c351aa4242c2719cbfbe671c09"
    algorithm: HS256
    key: "http://my-issuer.com"

```



Parameters available :

1) for plugin  -

   

   | FORM PARAMETER                                               | DESCRIPTION                                                  |
   | :----------------------------------------------------------- | ------------------------------------------------------------ |
   | `name` *required*                                            | The name of the plugin to use, in this case `jwt`.           |
   | `service.id`  **Type:** string                               | The ID of the Service the plugin targets.                    |
   | `route.id`  **Type:** string                                 | The ID of the Route the plugin targets.                      |
   | `enabled` *required*  **Type:** boolean  **Default value:** `true` | Whether this plugin will be applied.                         |
   | `config.uri_param_names` *optional*  **Type:** set of string elements  **Default value:** `jwt` | A list of querystring parameters that Kong will inspect to retrieve JWTs. |
   | `config.cookie_names` *optional*  **Type:** set of string elements | A list of cookie names that Kong will inspect to retrieve JWTs. |
   | `config.header_names` *optional*  **Type:** set of string elements  **Default value:** `authorization` | A list of HTTP header names that Kong will inspect to retrieve JWTs. |
   | `config.claims_to_verify` *optional*  **Type:** set of string elements | A list of registered claims (according to [RFC 7519](https://tools.ietf.org/html/rfc7519)) that Kong can verify as well. Accepted values: one of `exp` or `nbf`. |
   | `config.key_claim_name` *optional*  **Type:** string  **Default value:** `iss` | The name of the claim in which the `key` identifying the secret **must** be passed. Starting with version `0.13.1`, the plugin will attempt to read this claim from the JWT payload and the header, in that order. |
   | `config.secret_is_base64` *required*  **Type:** boolean  **Default value:** `false` | If true, the plugin assumes the credential’s `secret` to be base64 encoded. You will need to create a base64-encoded secret for your Consumer, and sign your JWT with the original secret. |
   | `config.anonymous` *optional*  **Type:** string              | An optional string (consumer uuid) value to use as an anonymous consumer if authentication fails. If empty (default), the request will fail with an authentication failure `4xx`. The anonymous value must refer to the Consumer `id` attribute that is internal to Kong, and **not** its `custom_id`. |
   | `config.run_on_preflight` *required*  **Type:** boolean  **Default value:** `true` | A boolean value that indicates whether the plugin should run (and try to authenticate) on `OPTIONS` preflight requests. If set to `false`, then `OPTIONS` requests will always be allowed. |
   | `config.maximum_expiration` *optional*  **Type:** number  **Default value:** `0` | A value between 0 and 31536000 (365 days) limiting the lifetime of the JWT to `maximum_expiration` seconds in the future. Any JWT that has a longer lifetime is rejected (HTTP 403). If this value is specified, `exp` must be specified as well in the `claims_to_verify` property. The default value of `0` represents an indefinite period. Potential clock skew should be considered when configuring this setting. |



2. for consumer -

   | PARAMETER                   | DESCRIPTION                                                  |
   | :-------------------------- | :----------------------------------------------------------- |
   | `username` *semi-optional*  | The username of the consumer. Either this field or `custom_id` must be specified. |
   | `custom_id` *semi-optional* | A custom identifier used to map the consumer to another database. Either this field or `username` must be specified. |



3. for JWT credential -

   | FIELD/PARAMETER             | DEFAULT | DESCRIPTION                                                  |
   | :-------------------------- | :------ | :----------------------------------------------------------- |
   | `{consumer}`                |         | The `id` or `username` property of the Consumer entity to associate the credentials to. |
   | `key` *optional*            |         | A unique string identifying the credential. If left out, it will be auto-generated. |
   | `algorithm` *optional*      | `HS256` | The algorithm used to verify the token’s signature. Can be `HS256`, `HS384`, `HS512`, `RS256`, or `ES256`. |
   | `rsa_public_key` *optional* |         | If `algorithm` is `RS256` or `ES256`, the public key (in PEM format) to use to verify the token’s signature. |
   | `secret` *optional*         |         | If `algorithm` is `HS256` or `ES256`, the secret used to sign JWTs for this credential. If left out, will be auto-generated. |



Here we can see that if algorithm is RS256 or ES256, we have to provide the `rsa_public_key` in pem format.

Lets understand rsa_public_key and secret usecase : 

There are two type of key encryption -

1. Symmetric Key Encryption - It only requires a single key for both encryption and decryption.
2. Asymmetric Key Encryption - It requires two key one to encrypt and the other one to decrypt. Two keys are also named as private key and public key.



We have used HS256, which is default algorithm as well, and we have passed secret which is  common for encrypting and decrypting out jwt. This is a example of symmetric key Encryption.



Now for asymmetric key :

```yaml
jwt_secrets:
  - consumer: login_server_issuer
  	key: "http://my-issuer.com"
    algorithm: RS256
    rsa_public_key: "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgK .... -----END PUBLIC KEY-----"
```

Here we have used `rsa_public_key` this has to be in pem format. To validate(decrypt) the JWT we have configured public key.

private key is used to craft JWT.

We should always rotate our keys for better security. But as these two keys are configured at different locations (owner and client side). It is difficult to sync the keys. To solve this problem we have jwk uri ( public keys url ) , which is also configured at owner side. Hence we can easily sync the keys.

Some of the OIDC provider like Microsoft Active Directory Federation Services (ADFS) provide us a JWK uri.But the format of the keys are in JWK ( Json web token ) format. We need a JWK to PEM conversion mechanism. We can write a script for the same in any programming language. I have used python script to convert JWK to PEM format.

```py
# jwk_pem.py
import jwcrypto.jwk as jwk
import requests
import argparse

arg_parser = argparse.ArgumentParser(
    description='JWK to PEM conversion tool')
arg_parser.add_argument('--jwk_uri',
                        dest='jwk_uri',
                        help='specify jwk uri',
                        required=True)
args = arg_parser.parse_args()                        
print("Fetching JWKS from {}".format(args.jwk_uri))
request = requests.get(args.jwk_uri, verify=False)
jwks = request.json()
key_json = jwks['keys'][0] 
key = jwk.JWK(**key_json)
pub_pem = key.export_to_pem()
print(pub_pem)
#f = open("out.txt", "w")
#f.write(pub_pem.decode())
#f.close()
```

Prerequisite :

we must install a `python_jwt` module using following command :

```sh
 python3 -m pip install --user python_jwt 
```



Usage :

```sh
python3 jwk_pem.py --jwk_uri <jwk-uri>
```



We can write the pem public key in out.txt as well if we want to load the pem in JWT plugin via file.

<h4> Kong JWT Signer :</h4>

The Kong JWT Signer plugin makes it possible to verify, sign, or re-sign one or two tokens in a request.

It is advanced version of JWT plugin which we have seen earlier.

It supports Dbless mode.

It is bundled with Kong Enterprise or Plus Version.

It has option to provide `config.access_token_jwks_uri`

Signature verification can be done with the plugin knowing about `config.access_token_jwks_uri`

