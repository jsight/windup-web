package org.jboss.windup.web.addons.websupport.rest.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateSessionFactoryModel;
import org.jboss.windup.web.addons.websupport.model.ReportFilterDTO;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class HibernateResourceImpl extends AbstractGraphResource implements HibernateResource
{
    @Override
    public Object getEntity(Long executionID, Map<String, Object> filterAsMap)
    {
        return this.getGraphData(executionID, filterAsMap, HibernateEntityModel.class, new ArrayList<>(Arrays.asList(
                    HibernateEntityModel.JPA_ENTITY_CLASS,
                    JavaClassModel.DECOMPILED_SOURCE)));
    }

    @Override
    public Object getMappingFile(Long executionID, Map<String, Object> filterAsMap)
    {
        return this.getGraphData(executionID, filterAsMap, HibernateMappingFileModel.class, new ArrayList<>());
    }

    @Override
    public Object getConfigurationFile(Long executionID, Map<String, Object> filterAsMap)
    {
        return this.getGraphData(executionID, filterAsMap, HibernateConfigurationFileModel.class, new ArrayList<>());
    }

    @Override
    public Object getSessionFactory(Long executionID, Map<String, Object> filterAsMap)
    {
        return this.getGraphData(executionID, filterAsMap, HibernateSessionFactoryModel.class, new ArrayList<>(Arrays.asList(
                    HibernateConfigurationFileModel.HIBERNATE_SESSION_FACTORY)));
    }

}
