const conceptTypes = {
  C: 'collection',
  G: 'granule',
  S: 'service',
  V: 'variable'
}

/**
 * Given a concept id, determine and return CMR concept type
 * @param {String} conceptId
 * @returns {String} concept type
 */
exports.getConceptType = (conceptId) => {
  const conceptKey = conceptId[0]

  return conceptTypes[conceptKey]
}
