package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.QueryConverter;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import okhttp3.OkHttpClient;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SearchServiceImpl<O extends ObjectType> extends CommonService<O> implements SearchService<O> {

    public SearchServiceImpl(ServiceContext context, Class<O> type) {
        super(context, type);
    }

    @Override
    public SearchResultList<O> list() throws ObjectNotFoundException, AuthenticationException {
        return list(null);
    }

    @Override
    public SearchResultList<O> list(ObjectQuery query) throws ObjectNotFoundException, AuthenticationException {
        return list(query, null);
    }

    @Override
    public SearchResultList<O> list(ObjectQuery query, Collection<SelectorOptions<GetOperationOptions>> options) throws ObjectNotFoundException, AuthenticationException {
        if (query == null) {
            query = prismContext().queryFactory().createQuery();
        }

        if (options == null) {
            options = new ArrayList<>();
        }

        try {
            QueryConverter converter = prismContext().getQueryConverter();
            QueryType queryType = converter.createQueryType(query);

            OkHttpClient client = client();

            // todo options

//            String path = ObjectTypes.getRestTypeFromClass(type()) + "/search";
//            client.replacePath(REST_PREFIX + "/" + path);
//
//            Response response = client.post(queryType);
//            validateResponse(response);
//
//            ObjectListType list = response.readEntity(ObjectListType.class);
//            // todo implement
//
//            return new SearchResultList<>((List<O>) list.getObject());
            return null;
        } catch (SchemaException ex) {
            throw new ClientException("Couldn't create query", ex);
        }
    }
}
