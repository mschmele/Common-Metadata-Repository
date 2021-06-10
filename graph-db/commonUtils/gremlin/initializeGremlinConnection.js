const gremlin = require('gremlin')

const { DriverRemoteConnection } = gremlin.driver
const { Graph } = gremlin.structure

let connection

/**
 * Connects to gremlin server to give reusable connection
 * @returns Gremlin traversal interface
 */
exports.initializeGremlinConnection = async () => {
  if (connection) {
    return connection
  }

  const gremlinUrl = process.env.GREMLIN_URL
  const dc = new DriverRemoteConnection(gremlinUrl, {})

  const graph = new Graph()
  connection = graph.traversal().withRemote(dc)

  return connection
}