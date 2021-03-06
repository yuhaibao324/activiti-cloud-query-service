/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.query.rest;

import com.querydsl.core.types.Predicate;
import org.activiti.cloud.services.query.app.repository.EntityFinder;
import org.activiti.cloud.services.query.app.repository.VariableRepository;
import org.activiti.cloud.services.query.model.Variable;
import org.activiti.cloud.services.query.resources.VariableResource;
import org.activiti.cloud.services.query.rest.assembler.VariableResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/" + VariableRelProvider.COLLECTION_RESOURCE_REL, produces = MediaTypes.HAL_JSON_VALUE)
public class VariableController {

    private final VariableRepository variableRepository;

    private VariableResourceAssembler variableResourceAssembler;

    private EntityFinder entityFinder;

    private PagedResourcesAssembler<Variable> pagedResourcesAssembler;

    @Autowired
    public VariableController(VariableRepository variableRepository,
                                         VariableResourceAssembler variableResourceAssembler,
                                         PagedResourcesAssembler<Variable> pagedResourcesAssembler, EntityFinder entityFinder) {
        this.variableRepository = variableRepository;
        this.variableResourceAssembler = variableResourceAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.entityFinder = entityFinder;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<VariableResource> findAll(@QuerydslPredicate(root = Variable.class) Predicate predicate,
                                                    Pageable pageable) {

        return pagedResourcesAssembler.toResource(variableRepository.findAll(predicate,
                                                                           pageable),
                                                  variableResourceAssembler);
    }

    @RequestMapping(value = "/{variableId}", method = RequestMethod.GET)
    public VariableResource findById(@PathVariable long variableId) {

        return variableResourceAssembler.toResource(entityFinder.findById(variableRepository,
                                                                          variableId,
                                                                          "Unable to find processInstance for the given id:'" + variableId + "'"));

    }

}
