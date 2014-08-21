package com.nhl.link.rest.runtime;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.CreateOrUpdateBuilder;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.3
 */
public abstract class BaseCreateOrUpdateBuilder<T> implements CreateOrUpdateBuilder<T> {

	protected CreateOrUpdateOperation operation;
	protected Class<T> type;
	private UriInfo uriInfo;
	protected Object id;

	protected Class<?> parentType;
	protected Object parentId;
	protected String relationshipFromParent;

	private IRequestParser requestParser;
	private IEncoderService encoderService;
	protected IMetadataService metadataService;
	private IConstraintsHandler constraintsHandler;

	private TreeConstraints readConstraints;
	private TreeConstraints writeConstraints;

	protected ObjectMapper mapper;

	public BaseCreateOrUpdateBuilder(Class<T> type, CreateOrUpdateOperation op, IEncoderService encoderService,
			IRequestParser requestParser, IMetadataService metadataService, IConstraintsHandler constraintsHandler) {
		this.type = type;
		this.operation = op;
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.metadataService = metadataService;
		this.constraintsHandler = constraintsHandler;
	}

	@Override
	public CreateOrUpdateBuilder<T> with(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return this;
	}

	@Override
	public CreateOrUpdateBuilder<T> id(Object id) {
		this.id = id;
		return this;
	}

	@Override
	public CreateOrUpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		this.parentType = parentType;
		this.parentId = parentId;
		this.relationshipFromParent = relationshipFromParent.getName();
		return this;
	}

	@Override
	public CreateOrUpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		this.parentType = parentType;
		this.parentId = parentId;
		this.relationshipFromParent = relationshipFromParent;
		return this;
	}

	@Override
	public CreateOrUpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public CreateOrUpdateBuilder<T> readConstraints(TreeConstraints constraints) {
		this.readConstraints = constraints;
		return this;
	}

	@Override
	public CreateOrUpdateBuilder<T> writeConstraints(TreeConstraints constraints) {
		this.writeConstraints = constraints;
		return this;
	}

	/**
	 * @since 1.4
	 */
	@Override
	public CreateOrUpdateBuilder<T> mapper(ObjectMapper mapper) {
		this.mapper = mapper;
		return this;
	}

	@Override
	public UpdateResponse<T> process(String entityData) {

		UpdateResponse<T> response = createResponse();

		// parse request
		requestParser.parseUpdate(response, uriInfo, entityData);

		processParent(response);

		// this handles single object update (or insert?)...
		processExplicitId(response);

		constraintsHandler.constrainUpdate(response, writeConstraints);

		// apply read constraints (TODO: should we only care about response
		// constraints after the commit?)
		constraintsHandler.constrainResponse(response, null, readConstraints);

		switch (operation) {
		case create:
			return withObjects(response, create(response), Status.CREATED);
		case createOrUpdate:
			return withObjects(response, createOrUpdate(response));
		case idempotentCreateOrUpdate:
			return withObjects(response, idempotentCreateOrUpdate(response));
		case update:
			return withObjects(response, update(response));
		default:
			throw new UnsupportedOperationException("Unsupported operation: " + operation);
		}
	}

	/**
	 * @since 1.4
	 */
	protected UpdateResponse<T> createResponse() {
		return new UpdateResponse<>(type);
	}

	private void processParent(UpdateResponse<T> response) {

		if (parentType != null || parentId != null || relationshipFromParent != null) {

			if (parentType == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent type is missing");
			}

			if (parentId == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent ID is missing");
			}

			if (relationshipFromParent == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent relationship is missing");
			}

			response.parent(parentType, parentId, relationshipFromParent);
		}
	}

	protected ObjRelationship relationshipFromParent() {
		if (parentType == null || relationshipFromParent == null) {
			return null;
		}

		return metadataService.getObjRelationship(parentType, relationshipFromParent);
	}

	protected void processExplicitId(UpdateResponse<T> response) {

		if (id != null) {
			processExplicitId(response, id);
		} else if (parentId != null) {
			ObjRelationship fromParent = relationshipFromParent();
			if (fromParent != null && fromParent.isToDependentEntity()) {
				processExplicitId(response, parentId);
			}
		}
	}

	private void processExplicitId(UpdateResponse<T> response, Object id) {

		// id was specified explicitly ... this means a few things:
		// * we expect zero or one object
		// * if zero - create an empty update that will be attached to the ID.
		// * if more than one - throw...

		if (response.getUpdates().isEmpty()) {
			response.getUpdates().add(new EntityUpdate());
		}

		response.getFirst().setId(id);
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects) {
		return withObjects(response, objects, Status.OK);
	}

	protected UpdateResponse<T> withObjects(UpdateResponse<T> response, List<T> objects, Status status) {
		return (UpdateResponse<T>) response.withObjects(objects).withEncoder(encoderService.makeEncoder(response))
				.withStatus(status);
	}

	protected abstract List<T> create(UpdateResponse<T> response);

	protected abstract List<T> createOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> idempotentCreateOrUpdate(UpdateResponse<T> response);

	protected abstract List<T> update(UpdateResponse<T> response);

}
