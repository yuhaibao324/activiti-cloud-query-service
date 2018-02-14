package org.activiti.cloud.services.query.security;

import com.querydsl.core.types.Predicate;
import org.activiti.engine.UserGroupLookupProxy;
import org.activiti.engine.UserRoleLookupProxy;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.cloud.services.SecurityPoliciesService;
import org.activiti.cloud.services.SecurityPolicy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SecurityPoliciesApplicationServiceTest {

    @InjectMocks
    @Spy
    private SecurityPoliciesApplicationService securityPoliciesApplicationService;

    @Mock
    private UserGroupLookupProxy userGroupLookupProxy;

    @Mock
    private UserRoleLookupProxy userRoleLookupProxy;

    @Mock
    private SecurityPoliciesService securityPoliciesService;

    @Mock
    private AuthenticationWrapper authenticationWrapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldNotModifyQueryWhenNoPoliciesDefined(){
        Predicate query = mock(Predicate.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(false);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        assertThat(securityPoliciesApplicationService.restrictProcessInstanceQuery(query, SecurityPolicy.READ)).isEqualTo(query);
    }

    @Test
    public void shouldNotModifyQueryWhenNoUser(){
        Predicate query = mock(Predicate.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn(null);

        assertThat(securityPoliciesApplicationService.restrictTaskQuery(query, SecurityPolicy.READ)).isEqualTo(query);
    }

    @Test
    public void shouldRestrictQueryWhenGroupsAndPoliciesAvailable(){
        Predicate query = mock(Predicate.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        when(userGroupLookupProxy.getGroupsForCandidateUser("bob")).thenReturn(Arrays.asList("hr"));

        Map<String, Set<String>> policies = new HashMap<String, Set<String>>();
        policies.put("rb1",new HashSet<>(Arrays.asList("SimpleProcess")));

        when(securityPoliciesService.getProcessDefinitionKeys(anyString(),
                anyCollection(), any(SecurityPolicy.class))).thenReturn(policies);

        securityPoliciesApplicationService.restrictVariableQuery(query, SecurityPolicy.READ);

        verify(securityPoliciesApplicationService,times(1)).addProcessDefRestrictionToExpression(any(),any(),any(),any());

    }

    @Test
    public void shouldHavePermissionWhenDefIsInPolicy(){
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");
        when(userRoleLookupProxy.isAdmin("bob")).thenReturn(false);

        when(userGroupLookupProxy.getGroupsForCandidateUser("bob")).thenReturn(groups);
        Map<String,Set<String>> map = new HashMap<String,Set<String>>();
        map.put("rb1",new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups,SecurityPolicy.WRITE)).thenReturn(map);
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups,SecurityPolicy.READ)).thenReturn(map);

        assertThat(securityPoliciesApplicationService.canWrite("key","rb1")).isTrue();
        assertThat(securityPoliciesApplicationService.canRead("key","rb1")).isTrue();
    }

    @Test
    public void shouldHavePermissionWhenAdmin(){

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("admin");
        when(userRoleLookupProxy.isAdmin("admin")).thenReturn(true);

        assertThat(securityPoliciesApplicationService.canWrite("key","rb1")).isTrue();
        assertThat(securityPoliciesApplicationService.canRead("key","rb1")).isTrue();
    }

    @Test
    public void shouldRestrictQueryWhenKeysFromPolicy(){
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");
        when(userRoleLookupProxy.isAdmin("bob")).thenReturn(false);

        when(userGroupLookupProxy.getGroupsForCandidateUser("bob")).thenReturn(groups);
        Map<String,Set<String>> map = new HashMap<String,Set<String>>();
        map.put("rb1",new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups,SecurityPolicy.READ)).thenReturn(map);

        Predicate query = mock(Predicate.class);
        securityPoliciesApplicationService.restrictProcessInstanceQuery(query, SecurityPolicy.READ);

        verify(securityPoliciesApplicationService,times(1)).addProcessDefRestrictionToExpression(any(),any(),any(),any());
    }
}