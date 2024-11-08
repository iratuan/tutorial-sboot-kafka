

### Requisições `CURL`

```text
curl -X POST http://localhost:8080/api/pedidos
-H "Content-Type: application/json"
-d '{
  "clienteId": 1,
  "pedidos": [
    {
      "codigoCliente": 1,
      "codigoProduto": 101,
      "quantidade": 2
    },
    {
     "codigoCliente": 1,
     "codigoProduto": 102,
     "quantidade": 1
    }
  ]
}'
{
  "clienteId": 1,
  "pedidos": [
    {
      "codigoCliente": 1,
      "codigoProduto": 1,
      "quantidade": 2
    },
    {
      "codigoCliente": 1,
      "codigoProduto": 3,
      "quantidade": 2
    }
  ]
}

```


```text

curl -X GET "http://localhost:8080/api/produtos/1" -H "Accept: application/json"

```