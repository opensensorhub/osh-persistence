/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.persistence.es;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.util.packed.PackedDataOutput;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.support.AbstractClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.sensorhub.api.persistence.DataKey;
import org.sensorhub.api.persistence.IDataFilter;
import org.sensorhub.api.persistence.IDataRecord;
import org.sensorhub.api.persistence.IMultiSourceStorage;
import org.sensorhub.api.persistence.IObsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.swe.v20.DataBlock;

/**
 * <p>
 * ES implementation of {@link IObsStorage} for storing observations.
 * </p>
 *
 * @author Mathieu Dhainaut <mathieu.dhainaut@gmail.com>
 * @since 2017
 */
public class ESMultiSourceStorageImpl extends ESObsStorageImpl implements IMultiSourceStorage<IObsStorage> {

	public ESMultiSourceStorageImpl() {

	}
	
	public ESMultiSourceStorageImpl(AbstractClient client) {
		super(client);
	}
	
	/**
	 * Class logger
	 */
	private static final Logger log = LoggerFactory.getLogger(ESMultiSourceStorageImpl.class);  
	
	@Override
	public Collection<String> getProducerIDs() {
		final SearchRequestBuilder scrollReq = client.prepareSearch(getLocalID())
				.setTypes(RS_DATA_IDX_NAME)
				.setQuery(QueryBuilders.existsQuery(PRODUCER_ID_FIELD_NAME))
		        .setScroll(new TimeValue(config.scrollMaxDuration));

		// wrap the request into custom ES Scroll iterator
		final Iterator<SearchHit> searchHitsIterator = new ESIterator(client, scrollReq,
				config.scrollFetchSize); //max of scrollFetchSize hits will be returned for each scroll
		
		Set<String> uniqueList = new HashSet<String>();
		while(searchHitsIterator.hasNext()) {
			SearchHit hit = searchHitsIterator.next();
			uniqueList.add(hit.getSource().get(PRODUCER_ID_FIELD_NAME).toString());
		}
		return uniqueList;
	}

	@Override
	public IObsStorage getDataStore(String producerID) {
		// return this because ES does not encapsulate any storage
		return this;
	}

	@Override
	public IObsStorage addDataStore(String producerID) {
		// return this because ES does not encapsulate any storage
		return this;
	}

	
}
