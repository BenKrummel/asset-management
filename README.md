# asset-management
Asset Management Sample Service


## Requirements

### Microsoft Sql Server

To run this project, you will need to add the following user to your Microsoft SQL Server Management Studio. 
```
username: super
password: yourStrong(!)Password
```
Or update the variables in the service/src/main/resources/application.yml

### Java

This project requires Java 11 to run.

## Run Locally

Clone the project

```bash
  git clone https://github.com/BenKrummel/asset-management.git
```

Go to the project directory

```bash
  cd asset-management
```

build the project

```bash
  gradlew build
```

Start the server. Default port is 8080

```bash
  gradlew bootrun
```

Start up kafka server. Default port is 9092
The kafka topic name is `asset.events.asset-promoted`

```bash
  docker-compose up
```

## API Reference

The api document can be found
`api/public-api-v1.yaml`

#### Get all assets

```http
  GET /v1/assets
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `pageSize` | `int` | the maximum number of entries to return. |
| `pageNumber` | `int` | the specific page of data to return|

#### Create asset

```http
  POST /v1/assets
```

| Body | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `Asset`      | `AssetModel` | **Required**. Asset Model Object to create on the backend entity.|

#### Get asset

```http
  GET /v1/assets/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of asset to fetch |

#### Update asset

```http
  PUT /v1/assets/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of asset to fetch |

| Body | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `Asset`      | `AssetModel` | **Required**. Asset Model Object to update the backend entity to.|

#### Delete asset
```http
  DELETE /v1/assets/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of asset to delete |
