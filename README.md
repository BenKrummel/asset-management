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
| `AssetList`      | `AssetList` | **Required**. List of assets that have the following structure.|

```json
{
  "ParentAsset": {
    "id": "8908a9e8-bb6d-11ed-afa1-0242ac120002",
    "promoted": false,
    "parentId": "abf944ce-68a8-40f9-b01f-636f6d28f6b8"
  },
  "ChildAssets": [
    {
      "id": "ad059da9-3f1b-49bb-8b0e-5be52f7d166e",
      "promoted": true,
      "parentId": "8908a9e8-bb6d-11ed-afa1-0242ac120002"
    },
    {
      "id": "3d57ba3b-84cc-4f29-aa45-cb50c24f4370",
      "promoted": false,
      "parentId": "8908a9e8-bb6d-11ed-afa1-0242ac120002"
    }
  ]
}
```

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
| `AssetList`      | `AssetList` | **Required**. Parent Asset is the asset that you are going to be updating. Parent asset is required child assets are not. Child assets can be used to assign assets that were previously assigned to a different asset. |

```json
{
  "ParentAsset": {
    "id": "8908a9e8-bb6d-11ed-afa1-0242ac120002",
    "promoted": false,
    "parentId": "abf944ce-68a8-40f9-b01f-636f6d28f6b8"
  },
  "ChildAssets": [
    {
      "id": "ad059da9-3f1b-49bb-8b0e-5be52f7d166e",
      "promoted": true,
      "parentId": "8908a9e8-bb6d-11ed-afa1-0242ac120002"
    },
    {
      "id": "3d57ba3b-84cc-4f29-aa45-cb50c24f4370",
      "promoted": false,
      "parentId": "8908a9e8-bb6d-11ed-afa1-0242ac120002"
    }
  ]
}
```

#### Delete asset
```http
  DELETE /v1/assets/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of asset to delete |
