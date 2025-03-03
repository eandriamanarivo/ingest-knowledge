package com.viewpoint.webapplication.services.applicatif.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.viewpoint.webapplication.constraints.converter.ResourceConverter;
import com.viewpoint.webapplication.data.constant.LabelConstant;
import com.viewpoint.webapplication.data.constant.SearchMethodConstant;
import com.viewpoint.webapplication.data.constant.UserGroupTypeConstant;
import com.viewpoint.webapplication.data.constant.ViewpointTypeConstant;
import com.viewpoint.webapplication.data.domainobject.NumericDocumentWebApp;
import com.viewpoint.webapplication.data.domainobject.ResourceCSV;
import com.viewpoint.webapplication.data.domainobject.ResourceWebApp;
import com.viewpoint.webapplication.data.domainobject.ViewpointWebApp;
import com.viewpoint.webapplication.data.dto.MultiStatusDTO;
import com.viewpoint.webapplication.data.dto.PreviewDto;
import com.viewpoint.webapplication.data.dto.ResourceScanDTO;
import com.viewpoint.webapplication.data.dto.ResponseDTO;
import com.viewpoint.webapplication.data.dto.folder.FolderDto;
import com.viewpoint.webapplication.data.dto.graph.GraphIUAParamDTO;
import com.viewpoint.webapplication.data.dto.method.BoundedMultiPathsNeighbourhood;
import com.viewpoint.webapplication.data.dto.method.SearchCompute;
import com.viewpoint.webapplication.data.dto.method.ShortestPathNeighbourhood;
import com.viewpoint.webapplication.data.dto.nextcloud.*;
import com.viewpoint.webapplication.data.dto.resourceliferay.NumericDocumentDto;
import com.viewpoint.webapplication.data.dto.user.UserFromLiferayDto;
import com.viewpoint.webapplication.data.entityclass.document.Activity;
import com.viewpoint.webapplication.data.entityclass.document.AuthorInfo;
import com.viewpoint.webapplication.data.entityclass.document.Tag;
import com.viewpoint.webapplication.data.entityclass.folder.ExternalFolder;
import com.viewpoint.webapplication.data.entityclass.graph.GraphEntity;
import com.viewpoint.webapplication.data.entityclass.group.RGroupEntity;
import com.viewpoint.webapplication.data.entityclass.resource.*;
import com.viewpoint.webapplication.data.entityclass.sync.HistorySync;
import com.viewpoint.webapplication.data.entityclass.userkg.UserKGResource;
import com.viewpoint.webapplication.data.entityclass.viewpoint.ViewpointEntity;
import com.viewpoint.webapplication.data.entityclass.viewpoint.ViewpointTypeEntity;
import com.viewpoint.webapplication.kernel.interpretation.FragmentedFeedbackInterpretation;
import com.viewpoint.webapplication.kernel.interpretation.Perspective;
import com.viewpoint.webapplication.services.applicatif.computemethod.LocalKMSA;
import com.viewpoint.webapplication.services.applicatif.computemethod.boundedmultipathsneighbourhood.BoundedMultiPathsNeighbourhoodSA;
import com.viewpoint.webapplication.services.applicatif.computemethod.shortestpathsneighbourhood.ShortestPathNeighbourhoodSA;
import com.viewpoint.webapplication.services.applicatif.computemethod.shortestpathsneighbourhood.ShortestPathSA;
import com.viewpoint.webapplication.services.applicatif.graph.GraphIUAParamSA;
import com.viewpoint.webapplication.services.applicatif.group.RGroupSA;
import com.viewpoint.webapplication.services.applicatif.useraction.UserActionSA;
import com.viewpoint.webapplication.services.applicatif.viewpoint.ViewpointSA;
import com.viewpoint.webapplication.services.dao.document.activity.ActivityDAO;
import com.viewpoint.webapplication.services.dao.document.custumProperty.CustomPropertyDAO;
import com.viewpoint.webapplication.services.dao.document.tag.SystemTagDAO;
import com.viewpoint.webapplication.services.dao.folder.ExternalFolderDAO;
import com.viewpoint.webapplication.services.dao.folder.UserFolderDAO;
import com.viewpoint.webapplication.services.dao.graph.GraphDao;
import com.viewpoint.webapplication.services.dao.group.RGroupDAO;
import com.viewpoint.webapplication.services.dao.resource.ResourceDao;
import com.viewpoint.webapplication.services.dao.resource.ResourceTypeDao;
import com.viewpoint.webapplication.services.dao.role.RoleRightsDao;
import com.viewpoint.webapplication.services.dao.synhistory.HistorySyncDao;
import com.viewpoint.webapplication.services.dao.user.UserDao;
import com.viewpoint.webapplication.services.dao.userkg.UserKgResourceDAO;
import com.viewpoint.webapplication.services.dao.userkg.UserKgViewpointDAO;
import com.viewpoint.webapplication.services.dao.viewpoint.ViewpointDao;
import com.viewpoint.webapplication.services.dao.viewpoint.ViewpointTypeDao;
import com.viewpoint.webapplication.services.repository.group.RGroupRepository;
import com.viewpoint.webapplication.services.repository.resource.ResourceRepository;
import com.viewpoint.webapplication.services.resourceMessage.DatabaseMessageSource;
import com.viewpoint.webapplication.ws.ws.GetWs;
import java.io.ByteArrayInputStream;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.viewpoint.webapplication.data.dto.graph.GraphIUAParamDTO;
import com.viewpoint.webapplication.services.applicatif.graph.GraphIUAParamSA;
import com.viewpoint.webapplication.services.applicatif.graph.GraphIUAParamSA;

/**
 * classe ResourceSAImpl contenant les attributs user, resourceRepository,
 * servletContext, manipule les types KnowledgeGraph et resource dans
 * GraphHolder
 *
 * @author a.safidy
 * @version 1.0
 */
@Service
public class ResourceSAImpl implements ResourceSA {

    @Autowired
    private ViewpointSA viewpointSA;

    @Autowired
    private UserActionSA userActionSA;

    @Autowired
    private ResourceConverter resourceConverter;

    @Autowired
    private ShortestPathNeighbourhoodSA spnSA;

    @Autowired
    private BoundedMultiPathsNeighbourhoodSA bmpnSA;

    @Autowired
    private ShortestPathSA shortestPathSA;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ResourceTypeDao resourceTypeDao;

    @Autowired
    private GraphDao graphDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private ViewpointDao viewpointDao;

    @Autowired
    private ViewpointTypeDao viewpointTypeDao;

    @Autowired
    private RGroupRepository rGroupRepository;

    @Autowired
    private HistorySyncDao historySyncDao;

    @Autowired
    private UserKgResourceDAO userKgResourceDAO;

    @Autowired
    private UserDao userDao;

    private Logger logger = Logger.getLogger(String.valueOf(getClass()));

    @Value("${liferay.portal.url}")
    private String liferayPortalUrl;

    @Value("${liferay.portal.user.login}")
    private String liferayPortalUserLogin;

    @Value("${liferay.portal.user.password}")
    private String liferayPortalUserPassword;

    @Value("${nextcloud.portal.url}")
    private String nextcloudPortalUrl;

    @Autowired
    private RGroupSA rGroupSA;

    @Autowired
    private DatabaseMessageSource messageSource;

    @Autowired
    private RoleRightsDao roleRightsDao;

    @Autowired
    private Perspective perspective;

    @Autowired
    private LocalKMSA localKMSA;

    @Autowired
    private ExternalFolderDAO externalFolderDAO;

    @Autowired
    private UserFolderDAO userFolderDAO;

    @Autowired
    private UserKgViewpointDAO userKgViewpointDAO;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RGroupDAO rGroupDAO;

    @Autowired
    private CustomPropertyDAO customPropertyDAO;

    @Value("${role.vwa.admin}")
    private String roleVwaAdmin;

    @Autowired
    private GraphIUAParamSA graphIUAParamSA;


    @Autowired
    private SystemTagDAO systemTagDAO;



    @Autowired
    private ActivityDAO activityDAO;

//    @Value("${nextcloud.bas-url}")
//    private String Base_URL;

    @Value("${nextcloud.api.url.activities}")
    private String Activities;

    @Value("${nextcloud.aplogin.name}")
    private String username;

    @Value("${nextcloud.aplogin.pwd}")
    private String password;

    private final OkHttpClient client = new OkHttpClient();


    @Value("${nextcloud.webdav.url}")
    private String Webdav ;

    @Value("${nextcloud.api.url.tags}")
    private String Tags;

    private static final Logger LOGGER = Logger.getLogger(ResourceSAImpl.class.getName());

    @Value("${nextcloud.api.url.folders}")
    private String NEXTCLOUD_FOLDERS_URL;


    private OkHttpClient httpClient;

    @Value("${nextcloud.bas-url}")
    private String nextcloudUrl;
    /**
     * recherche du plus court chemin
     *
     * @param label1 : label d'une ressource1
     * @param label2 : labe d'une ressource2
     * @param username : identifie le resurce a observer
     * @param kgLabel : label du graphe étudié
     * @return jsonpath chemin trouvé
     */
    @Override
    public String shortestPath(String label1, String label2, String username, String kgLabel) {
        try {
            ResourceEntity resAgent = resourceDao.getResourceByLabel(username);
            perspective.setInterpretationFunction(new FragmentedFeedbackInterpretation());

            ResourceEntity resource1 = resourceDao.getResourceByLabel(label1);
            ResourceEntity resource2 = resourceDao.getResourceByLabel(label2);

            GraphEntity graph = graphDao.getGraphByName(kgLabel);

            if (resource1 != null && resource2 != null) {
                String jsonPath = shortestPathSA.getJSONShortestPath(resource1, resource2,
                        servletContext.getContextPath(), graph);
                return jsonPath;
            }
        } catch (Exception e) {
            return "NOPATH";
        }
        return "";
    }

    @Override
    public LinkedList<String> shortestPathMultisearch(String label1, String label2, String username, float m, float p, String deb, String fin, String kgLabel, Long agroup, int filter, int filterUserGroup, Long userGroup) {
        List<String> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        LinkedList<String> multiSearch = rGroupSA.computeGroup(labelList, username, SearchMethodConstant.SPN, m, p, deb, fin, kgLabel, filter, agroup, filterUserGroup, userGroup);
        String shortestPath = this.shortestPath(label1, label2, username, kgLabel);
        multiSearch.add(0, shortestPath);
        return multiSearch;
    }

    /**
     * retourne une liste de nom d'agent compris dans un graphe selon le kgLabel
     * contenu dans GraphHolder
     *
     * @param kgLabel: label d'un agent
     * @return name : nom retourné
     */
    @Override
    public List<String> agentNames(String kgLabel) {
        GraphEntity graphEntity = graphDao.getGraphByName(kgLabel);
        if (graphEntity != null) {
            return resourceDao.getResourceLabels();
        }
        return new ArrayList<>();
    }

    /**
     * retourne une liste de nom compris dans un graphe selon son kgLabel
     *
     * @param kgLabel : label du graphe de connaissance
     * @return ArrayList de nom
     */
    @Override
    public List<String> names(String kgLabel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        GraphEntity graph = graphDao.getGraphByName(kgLabel);

        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new ArrayList();
        }
        return this.getResourceLabelsWithVisibility(resourceDao.getResourceLabels(), user);
    }

    @Override
    public List<String> names(String kgLabel, List<String> type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        GraphEntity graph = graphDao.getGraphByName(kgLabel);

        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new ArrayList();
        }

        if (CollectionUtils.isEmpty(type)) {
            return this.getResourceLabelsWithVisibility(resourceDao.getResourceLabels(), user);
        } else {
            if (type.contains("UserGroupFactual") || type.contains("UserGroupSubjective")) {
                List<String> typeUserGroups = new ArrayList<>();
                List<String> types = new ArrayList<>();
                for (String t : type) {
                    if (t.equals("UserGroupFactual") || t.equals("UserGroupSubjective")) {
                        typeUserGroups.add(t.replace("UserGroup", ""));
                    } else {
                        types.add(t);
                    }
                }
                List<String> userGroupNames = resourceDao.getUserGroupByType(typeUserGroups);
                List<String> resourceLabelsWithoutUserGroup = this.getResourceLabelsWithVisibility(resourceDao.getResourceLabels(types), user);
                if (!Objects.isNull(userGroupNames)) {
                    resourceLabelsWithoutUserGroup.addAll(userGroupNames);
                }
                return resourceLabelsWithoutUserGroup;
            } else {
                return this.getResourceLabelsWithVisibility(resourceDao.getResourceLabels(type), user);
            }
        }
    }

    private List<String> getResourceLabelsWithVisibility(List<String> resources, ResourceEntity user) {
        List<ExternalFolder> foldersByUser = userFolderDAO.getFoldersByUser(user);
        return resources
                .stream()
                .filter(resource -> filterByFolderUser(foldersByUser, resource))
                .collect(Collectors.toList());
    }

    private boolean filterByFolderUser(List<ExternalFolder> foldersByUser, String resource) {
        ResourceEntity entity = resourceDao.getResourceByLabel(resource);
        if (Objects.isNull(entity)) {
            return false;
        }
        if (entity instanceof NumericDocumentEntity) {
            NumericDocumentEntity document = (NumericDocumentEntity) entity;
            if (Objects.nonNull(document.getFolder())) {
                if (!foldersByUser.contains(document.getFolder())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * retourne le nom d'un graphe de connaissance
     *
     * @return name
     */
    @Override
    public List<String> kgs(boolean demo) {
        return graphDao.getGraphNameList(demo);
    }

    @Override
    public List<GraphEntity> findAllKg(boolean demo) {
        return graphDao.findAll(demo);
    }

    @Override
    public List<GraphEntity> findAllKgForUserConnected(UserEntity userEntity, boolean demo) {
        boolean isHaAdmin = roleRightsDao.isUserHasRoleCode(userEntity, roleVwaAdmin);
        if (isHaAdmin) {
            return this.findAllKg(demo);
        } else {
            List<GraphEntity> graphEntities = graphDao.findGraphEntityInUserRightsByUser(userEntity);
            if (graphEntities.isEmpty()) {
                return this.findAllKg(demo);
            }
            return graphEntities;
        }
    }

    /**
     * @param label : label d'une resoource recherché
     * @param username : user de la resoource
     * @param kgLabel : label du graphe
     * @return localKM.getLocalKM(search, 12)
     */
    @Override
    public String localKM(String label, String username, String kgLabel) {
        ResourceEntity search = resourceDao.getResourceByLabel(label);
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        if (search != null) {
            localKMSA.setUrlContext(servletContext.getContextPath());
            return localKMSA.getLocalKM(search, 20, graph);
        }

        return null;
    }

    @Override
    public LinkedList<String> search(String label, String username, String method, float m, float p, String deb,
                                     String fin, String kgLabel) {
        return this.search(label, username, method, m, p, deb, fin, kgLabel, 0, -1l);
    }

    @Override
    public void updateSrenths(String kgLabel, HashMap<String, Float> typesStrenghts) {
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        perspective.updateSrenths(graph, typesStrenghts);
    }


    @Override
    public LinkedList<String> searchStepByStep(
            String label,
            String username,
            String method,
            float m,
            float p,
            String deb,
            String fin,
            String kgLabel,
            Long resUserGroup,
            int filterUserGroup,
            int max_answer,
            boolean isSearchStepByStep
    ) {
        System.out.println("searchStepByStep => label: " + label + ", username: " + username + ", method: " + method +
                ", m: " + m + ", p: " + p + ", deb: " + deb + ", fin: " + fin +
                ", kgLabel: " + kgLabel + ", resUserGroup: " + resUserGroup +
                ", filterUserGroup: " + filterUserGroup + ", max_answer: " + max_answer +
                ", isSearchStepByStep: " + isSearchStepByStep);
        GraphIUAParamDTO graphIuaParam = graphIUAParamSA.getGraphEntityByKg(kgLabel);
        double radius_increment = 0.5d;
        double max_radius = 2d;
        if (graphIuaParam != null) {
            if (graphIuaParam.getRadiusIncrement() != null) {
                radius_increment = graphIuaParam.getRadiusIncrement();
            }
            if (graphIuaParam.getMaxRadius() != null) {
                max_radius = graphIuaParam.getMaxRadius();
            }

        }
        int answer = 0;
        LinkedList<String> result = new LinkedList<>();

        if (isSearchStepByStep) {
            double radius = radius_increment;
            while ((radius <= max_radius) && (answer <= max_answer)) {
                result = search(label, username, method, (float) radius, p, deb, fin,
                        kgLabel, filterUserGroup, resUserGroup);

                radius = radius + radius_increment;
                answer = result.size() - 1;
            }
        }

        return result;
    }



//    @Override
//    public LinkedList<String> searchStepByStep(
//            String label,
//            String username,
//            String method,
//            float m,
//            float p,
//            String deb,
//            String fin,
//            String kgLabel,
//            Long resUserGroup,
//            int filterUserGroup,
//            int max_answer,
//            boolean isSearchStepByStep
//    ) {
//        System.out.println("searchStepByStep => label: " + label + ", username: " + username + ", method: " + method +
//            ", m: " + m + ", p: " + p + ", deb: " + deb + ", fin: " + fin +
//            ", kgLabel: " + kgLabel + ", resUserGroup: " + resUserGroup +
//            ", filterUserGroup: " + filterUserGroup + ", max_answer: " + max_answer +
//            ", isSearchStepByStep: " + isSearchStepByStep);
//        GraphIUAParamDTO graphIuaParam = graphIUAParamSA.getGraphEntityByKg(kgLabel);
//        double radius_increment = 0.5d;
//        double max_radius = 2d;
//        if (graphIuaParam != null) {
//            if (graphIuaParam.getRadiusIncrement() != null) {
//                radius_increment = graphIuaParam.getRadiusIncrement();
//            }
//            if (graphIuaParam.getMaxRadius() != null) {
//                max_radius = graphIuaParam.getMaxRadius();
//            }
//
//        }
//        int answer = 0;
//        LinkedList<String> result = new LinkedList<>();
//
//        if (isSearchStepByStep) {
//            double radius = radius_increment;
//            while ((radius <= max_radius) && (answer <= max_answer)) {
//                result = search(label, username, method, (float) radius, p, deb, fin,
//                        kgLabel, filterUserGroup, resUserGroup);
//
//                radius = radius + radius_increment;
//                answer += result.size() - 1;
//            }
//        }
//
//        return result;
//    }

    /**
     * liste de recherche de ressource ayant le label en parametre
     *
     * @param label :label d'une ressource recherché dans le graphe
     * @param username : ressource observée
     * @param method : methode utilisé pour la recherche
     * @param m : valeur de type float
     * @param p : valeur de type float
     * @param kgLabel : label du graphe
     * @return nei liste de ressource de type LinkedList
     */
    @Override
    public LinkedList<String> search(String label, String username, String method, float m, float p, String deb, String fin, String kgLabel, int filterUserGroup, Long resUserGroup) {

        System.out.println("search => ["
                + "label='" + label + "'"
                + ", method='" + method + "'"
                + ", kgLabel='" + kgLabel + "'"
                + ", deb='" + deb + "'"
                + ", fin='" + fin + "'"
                + ", UserGroupFilterID='" + resUserGroup + "'"
                + ", UGPolarity='" + filterUserGroup + "'"
                + ", radius='" + m + "'"
                + ", p='" + p + "'"
                + "]");

        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        ResourceEntity resAgent = resourceDao.getResourceByLabel(username);
        perspective.setInterpretationFunction(new FragmentedFeedbackInterpretation());
        ResourceEntity search = resourceDao.getResourceByLabel(label);
        ResourceEntity resUserGroupEntity = resourceDao.getResourceById(resUserGroup);

        List<ResourceEntity> userFromUserGroupEntity = null;
        if (!Objects.isNull(resUserGroupEntity)) {
            userFromUserGroupEntity = this.retrieveUserFromGraphAndUserGroup(graph, resUserGroupEntity);
        }
        if (search == null) {
            logger.info("search is null");
            return null;
        } else {

            SearchCompute sc = null;
            TreeMap<ResourceEntity, Float> neighbourhood = new TreeMap<>();
            // neighbourhood.put(search, 0.0f);
            Date begin = null;
            Date end = null;
            if (!StringUtils.isEmpty(deb)) {
                try {
                    begin = DateUtils.parseDate(deb, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    begin = null;
                }
            }
            if (!StringUtils.isEmpty(fin)) {
                try {
                    end = DateUtils.parseDate(fin, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    end = null;
                }
            }
            try {
                switch (method) {
                    case SearchMethodConstant.SPN:
                        ShortestPathNeighbourhood spn = new ShortestPathNeighbourhood();
                        spn.setObserver(resAgent);
                        spnSA.setSubjectiveMethod(spn);
                        spnSA.setGraph(graph);
                        neighbourhood = spnSA.computeAndSort(search,
                                m,
                                begin,
                                end,
                                !Objects.isNull(resUserGroupEntity) ? filterUserGroup : 0,
                                !Objects.isNull(resUserGroupEntity) ? userFromUserGroupEntity : new ArrayList<>()
                        );
                        sc = spnSA.getComputeResult();
                        break;
                    case SearchMethodConstant.BMPD:
                        BoundedMultiPathsNeighbourhood bmpn = new BoundedMultiPathsNeighbourhood();
                        bmpn.setObserver(resAgent);
                        bmpnSA.setSubjectiveMethod(bmpn);
                        bmpnSA.setGraph(graph);

                        neighbourhood = bmpnSA.computeAndSort(search,
                                m,
                                begin,
                                end,
                                !Objects.isNull(resUserGroupEntity) ? filterUserGroup : 0,
                                !Objects.isNull(resUserGroupEntity) ? userFromUserGroupEntity : new ArrayList<>()
                        );

                        sc = bmpnSA.getComputeResult();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LinkedList<String> nei = new LinkedList<>();
            if (sc == null) {
                return nei;
            }
            nei.add(sc.getKmJson());

            nei.add(search + "\\" + 0.0f);
            for (Map.Entry<ResourceEntity, Float> neighbour : neighbourhood.entrySet()) {
                if (neighbour.getValue() > 0.0f) {
                    nei.addLast(neighbour.getKey().toString() + "\\" + neighbour.getValue());
                }
            }

            return nei;
        }
    }

    /**
     * liste de recherche de ressource ayant le label en parametre
     *
     * @param label :label d'une ressource recherché dans le graphe
     * @param username : ressource observée
     * @param method : methode utilisé pour la recherche
     * @param m : valeur de type float
     * @param p : valeur de type float
     * @param kgLabel : label du graphe
     * @return nei liste de ressource de type LinkedList
     */
    @Override
    public Map<String, Float> computeAndshort(String label, String username, String method, float m, float p,
                                              String deb, String fin, String kgLabel, int filterUserGroup, Long userGroup) {

        logger.info("search => [label='" + label + "', method='" + method + "', kgLabel='" + kgLabel + "']");
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        ResourceEntity resAgent = resourceDao.getResourceByLabel(username);
        perspective.setInterpretationFunction(new FragmentedFeedbackInterpretation());
        ResourceEntity search = resourceDao.getResourceByLabel(label);
        ResourceEntity resUserGroupEntity = resourceDao.getResourceById(userGroup);
        List<ResourceEntity> userFromUserGroupEntity = null;
        if (!Objects.isNull(resUserGroupEntity)) {
            userFromUserGroupEntity = this.retrieveUserFromGraphAndUserGroup(graph, resUserGroupEntity);
        }
        if (search == null) {
            logger.info("search is null");
            return null;
        } else {

            SearchCompute sc = null;
            TreeMap<ResourceEntity, Float> neighbourhood = new TreeMap<>();
            // neighbourhood.put(search, 0.0f);
            Date begin = null;
            Date end = null;
            if (!StringUtils.isEmpty(deb)) {
                try {
                    begin = DateUtils.parseDate(deb, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    begin = null;
                }
            }
            if (!StringUtils.isEmpty(fin)) {
                try {
                    end = DateUtils.parseDate(fin, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    end = null;
                }
            }
            try {
                switch (method) {
                    case SearchMethodConstant.SPN:
                        ShortestPathNeighbourhood spn = new ShortestPathNeighbourhood();
                        spn.setObserver((UserEntity) resAgent);
                        spnSA.setSubjectiveMethod(spn);
                        neighbourhood = spnSA.computeAndSort(search,
                                m,
                                begin,
                                end,
                                !Objects.isNull(resUserGroupEntity) ? filterUserGroup : 0,
                                !Objects.isNull(resUserGroupEntity) ? userFromUserGroupEntity : new ArrayList<>()
                        );
                        break;
                    case SearchMethodConstant.BMPD:
                        BoundedMultiPathsNeighbourhood bmpn = new BoundedMultiPathsNeighbourhood();
                        bmpn.setObserver((UserEntity) resAgent);
                        bmpnSA.setSubjectiveMethod(bmpn);
                        neighbourhood = bmpnSA.computeAndSort(search,
                                m,
                                begin,
                                end,
                                !Objects.isNull(resUserGroupEntity) ? filterUserGroup : 0,
                                !Objects.isNull(resUserGroupEntity) ? userFromUserGroupEntity : new ArrayList<>()
                        );
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Float> res = new HashMap<>();
            for (Map.Entry<ResourceEntity, Float> neighbour : neighbourhood.entrySet()) {
                if (neighbour.getValue() > 0.0f) {
                    res.put(neighbour.getKey().getLabel(), neighbour.getValue());
                }
            }
            return res;
        }
    }

    @Override
    public Map<String, Float> computeAndshort(String label, String username, String method, float m, float p,
                                              String deb, String fin, String kgLabel) {
        logger.info("search => [label='" + label + "', method='" + method + "', kgLabel='" + kgLabel + "']");
        ResourceEntity resAgent = resourceDao.getResourceByLabel(username);
        perspective.setInterpretationFunction(new FragmentedFeedbackInterpretation());
        ResourceEntity search = resourceDao.getResourceByLabel(label);

        if (search == null) {
            return null;
        } else {

            SearchCompute sc = null;
            TreeMap<ResourceEntity, Float> neighbourhood = new TreeMap<>();
            // neighbourhood.put(search, 0.0f);
            Date begin = null;
            Date end = null;
            if (!StringUtils.isEmpty(deb)) {
                try {
                    begin = DateUtils.parseDate(deb, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    begin = null;
                }
            }
            if (!StringUtils.isEmpty(fin)) {
                try {
                    end = DateUtils.parseDate(fin, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } catch (Exception e) {
                    end = null;
                }
            }
            try {
                switch (method) {
                    case SearchMethodConstant.SPN:
                        ShortestPathNeighbourhood spn = new ShortestPathNeighbourhood();
                        spn.setObserver((UserEntity) resAgent);
                        spnSA.setSubjectiveMethod(spn);
                        neighbourhood = spnSA.computeAndSort(search, m, begin, end);
                        break;
                    case SearchMethodConstant.BMPD:
                        BoundedMultiPathsNeighbourhood bmpn = new BoundedMultiPathsNeighbourhood();
                        bmpn.setObserver((UserEntity) resAgent);
                        bmpnSA.setSubjectiveMethod(bmpn);
                        neighbourhood = bmpnSA.computeAndSort(search, m, begin, end);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Float> res = new HashMap<>();
            for (Map.Entry<ResourceEntity, Float> neighbour : neighbourhood.entrySet()) {
                if (neighbour.getValue() > 0.0f) {
                    res.put(neighbour.getKey().getLabel(), neighbour.getValue());
                }
            }
            return res;
        }
    }

    private ViewpointWebApp createViewpointCreateionWebAppFromResourceWepApp(ResourceWebApp resource, String kgLabel) {
        ViewpointWebApp viewpointWebApp = new ViewpointWebApp();
        viewpointWebApp = new ViewpointWebApp();
        viewpointWebApp.setEmitter(resource.getEmitter());
        viewpointWebApp.setResource1(resource.getEmitter());
        viewpointWebApp.setResource2(resource.getLabel());
        viewpointWebApp.setType(ViewpointTypeConstant.DYNAMIC_CREATE);
        viewpointWebApp.setTargetKG(kgLabel);
        return viewpointWebApp;
    }

    private ViewpointWebApp createViewpointWebAppToResourceCsv(ResourceCSV resource, String kgLabel, String emitter) {
        ViewpointWebApp viewpointWebApp = new ViewpointWebApp();
        viewpointWebApp = new ViewpointWebApp();
        viewpointWebApp.setEmitter(emitter);
        viewpointWebApp.setResource1(emitter);
        viewpointWebApp.setResource2(resource.getLabel());
        viewpointWebApp.setType(ViewpointTypeConstant.FACTUAL);
        viewpointWebApp.setTargetKG(kgLabel);
        return viewpointWebApp;
    }

    private String addResource(ResourceWebApp resource, boolean isDemo) {
        String demo = (isDemo) ? " demo => " : " => ";
        logger.info("addResource" + demo + resource.toString());
        ResourceEntity research = resourceDao.getResourceByLabel(resource.getLabel());
        if (resource.getId() != null) {
            research = resourceDao.getResourceById(Long.valueOf("" + resource.getId()));
        }
        String kgLabel = resource.getTargetKG();
        if (research == null) {
            if ("CREATE".equals(resource.getAction())) {
                try {
                    if (isDemo) {
                        String kgDemo = "KG" + RequestContextHolder.currentRequestAttributes().getSessionId();
                        if (kgDemo.equals(resource.getTargetKG())) {
                            //ResourceEntity resKernel = resourceRepository.saveResource(resource);
                        } else {
                            return messageSource.resolveCode("RESSOURCE_CREATION_HAS_NOT_PERMISSION",
                                    LabelConstant.RESSOURCE_CREATION_HAS_NOT_PERMISSION);
                        }
                        String viewpointCreation = viewpointSA.addViewPointDemo(createViewpointCreateionWebAppFromResourceWepApp(resource, kgLabel));
                        if (viewpointCreation.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                            return messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED);
                        }
                    } else {

                        GraphEntity graph = graphDao.getGraphByName(resource.getTargetKG());
                        if (Objects.isNull(graph)) {
                            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
                        }
                        ResourceEntity resEntity = resourceConverter.convertWebAppToEntity(resource);
                        ResourceEntity testEntity = resourceDao.getResourceByLabel(resEntity.getLabel());
                        if (testEntity == null) {
                            resourceDao.saveResource(resEntity);
                            resEntity = resourceDao.getResourceByLabel(resource.getLabel());
                            userKgResourceDAO.save(new UserKGResource(resEntity.getCreator(), graph, resEntity));
                        }
                        String viewpointCreation = viewpointSA.addViewPoint(createViewpointCreateionWebAppFromResourceWepApp(resource, kgLabel));
                        if (viewpointCreation.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                            return messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED);
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR + " ===> " + e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR + " ===>>" + ex);
                }

            } else {
                return messageSource.resolveCode("RESOURCE_NOT_EXIST", LabelConstant.RESOURCE_NOT_EXIST);
            }
        } else if ("CREATE".equals(resource.getAction())) {
            return messageSource.resolveCode("RESOURCE_ALREADY_EXISTS", LabelConstant.RESOURCE_ALREADY_EXISTS);
        } else {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                ResourceEntity user = resourceDao.getResourceByLabel(username);
                if (Objects.isNull(user)) {
                    return messageSource.resolveCode("RESOURCE_EDIT_ERROR", LabelConstant.RESOURCE_EDIT_ERROR);
                }
                ResourceEntity testEntity = resourceDao.getResourceById((long) resource.getId());
                if (!testEntity.getCreator().equals(user)) {
                    return messageSource.resolveCode("RESOURCE_NOT_CREATOR", LabelConstant.RESOURCE_NOT_CREATOR);
                }
                if (testEntity != null) {
                    testEntity.setDescription(resource.getDescription());
                    testEntity.setLabel(resource.getLabel());
                    testEntity.setPreviewUrl(resource.getPreviewUrl());
                    resourceDao.saveResource(testEntity);
                }
            } catch (Exception e) {
                return messageSource.resolveCode("RESOURCE_EDIT_ERROR", LabelConstant.RESOURCE_EDIT_ERROR + " <====> " + e);
            }
        }
        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
    }

    @Override
    public String deleteResource(List<Long> ids) {
        String result = this.confirmResourceRight(ids);
        if (StringUtils.contains(result, messageSource.resolveCode("RESOURCE_NOT_CREATOR", LabelConstant.RESOURCE_NOT_CREATOR))
                || StringUtils.contains(result, messageSource.resolveCode("RESOURCE_DELETE_ASKING_ERROR", LabelConstant.RESOURCE_DELETE_ASKING_ERROR))) {
            return result;
        }
        List<ResourceEntity> resources = ids
                .stream()
                .map(id -> new ResourceEntity(id))
                .collect(Collectors.toList());
        try {
            viewpointSA.deleteViewpointsFromResource(resources);
            userKgResourceDAO.deleteByResourceByIds(resources);
            rGroupDAO.deleteGroupItemByResources(resources);
            resourceRepository.deleteResourceWithIds(ids);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ResourceSAImpl.class.getName()).log(Level.SEVERE, null, e);
            return e.getMessage();
        }

        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_DELETED", LabelConstant.RESOURCE_SUCCESSFULLY_DELETED);
    }

    @Override
    public Set<String> getHistoryVisuInitByKgLabel(String kgLabel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(user)) {
            return new HashSet<>();
        }
        GraphEntity graphEntity = graphDao.getGraphByName(kgLabel);
        Set<String> ressourcesLabel = new HashSet<>();

        if (Objects.nonNull(graphEntity)) {
            if (Objects.nonNull(graphEntity.getVisuInit()) && graphEntity.getVisuInit() <= 0) {
                //DO NOTHING IF 0
            } else {
//                List<ViewpointEntity> viewpointEntities = viewpointDao.getViewpointByGraphInLimit(graphEntity, Objects.nonNull(graphEntity.getVisuInit()) ? graphEntity.getVisuInit() : 1);
                List<ViewpointEntity> viewpointVisibleByUser = viewpointDao.checkViewpointNumericDocumentVisibilityWithGraph(graphEntity, user.getId());
                if (!viewpointVisibleByUser.isEmpty()) {
                    for (ViewpointEntity viewpointEntity : viewpointVisibleByUser) {
                        if (Objects.nonNull(viewpointEntity.getResource1())) {
                            ResourceEntity resourceEntity = viewpointEntity.getResource1();
                            ressourcesLabel.add(resourceEntity.getLabel());
                        }
                        if (Objects.nonNull(viewpointEntity.getResource2())) {
                            ResourceEntity resourceEntity = viewpointEntity.getResource2();
                            ressourcesLabel.add(resourceEntity.getLabel());
                        }
                    }
                }
            }
        }
        return ressourcesLabel;
    }

    @Override
    public ResponseDTO getDefaultRadius(String kgLabel) {
        GraphEntity graphEntity = graphDao.getGraphByName(kgLabel);
        ResponseDTO responseDTO = new ResponseDTO();
        Integer defaultRadius = Objects.nonNull(graphEntity) && Objects.nonNull(graphEntity.getDefaultRadius())
                ? graphEntity.getDefaultRadius() : 2;
        responseDTO.setDefaultRadius(defaultRadius);
        return responseDTO;
    }

    @Override
    public String addResourceCsv(ResourceCSV resource) {
        ResourceEntity research = resourceDao.getResourceByLabel(resource.getLabel());
        ResourceEntity researchId = resourceDao.getResourceById(resource.getId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
        }
        String emitter = authentication.getName();
        String kgLabel = resource.getTargetKG();
        if (research == null && researchId == null) {
            try {
                ResourceEntity resKernel = resourceConverter.convertCsvToEntity(resource);
                if (resKernel != null) {
                    ResourceEntity testEntity = resourceDao.getResourceByLabel(resKernel.getLabel());
                    ResourceEntity testEnityId = resourceDao.getResourceById(resKernel.getId());
                    if (testEntity == null && testEnityId == null) {
                        ResourceEntity user = resourceDao.getResourceByLabel(emitter);
                        GraphEntity graph = graphDao.getGraphByName(kgLabel);
                        if (Objects.isNull(user) || Objects.isNull(graph)) {
                            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
                        }
                        resKernel.setId(null);
                        resourceDao.saveResource(resKernel);
                        resKernel = resourceDao.getResourceByLabel(resKernel.getLabel());
                        ExternalFolder folder = null;
                        if (resKernel.getType().getName().equals("NumericDocument")) {
                            folder = externalFolderDAO.findByLabel(resource.getFolder());
                        }
                        UserKGResource userKGResource = new UserKGResource(user, graph, resKernel);
                        if (Objects.nonNull(folder)) {
                            if (Objects.isNull(userFolderDAO.findByUserAndFolder(user, folder))) {
                                userKGResource.setIsVisible(Boolean.FALSE);
                            }
                        }
                        userKgResourceDAO.save(userKGResource);
                    }
                    if (resource.getType().equals("ArtificialAgent")) {
                        String viewpointCreation = viewpointSA.addViewPoint(createViewpointWebAppToResourceCsv(resource, kgLabel, emitter));
                        if (viewpointCreation.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                            return messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED);
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
            } catch (Exception ex) {
                ex.printStackTrace();
                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
            }

        } else {
            ResourceEntity resKernel = null;
            try {
                resKernel = resourceConverter.convertCsvToEntity(resource);
                resourceDao.saveResource(resKernel);
                resKernel = resourceDao.getResourceByLabel(resKernel.getLabel());
                ResourceEntity user = resourceDao.getUserByLabel(emitter);
                GraphEntity graph = graphDao.getGraphByName(kgLabel);
                UserKGResource ukgr = userKgResourceDAO.findByUserAndGraphAndResource(user, graph, resKernel);
                if (Objects.isNull(ukgr)) {
                    ukgr = new UserKGResource();
                    ukgr.setGraph(graph);
                    ukgr.setResource(resKernel);
                    ukgr.setUser(user);
                    ukgr.setIsVisible(Boolean.TRUE);
                }
                ExternalFolder folder = null;
                if (resKernel.getType().getName().equals("NumericDocument")) {
                    folder = externalFolderDAO.findByLabel(resource.getFolder());
                }
                if (Objects.nonNull(folder)) {
                    if (Objects.isNull(userFolderDAO.findByUserAndFolder(user, folder))) {
                        ukgr.setIsVisible(Boolean.FALSE);
                    } else {
                        ukgr.setIsVisible(Boolean.TRUE);
                    }
                }
                userKgResourceDAO.save(ukgr);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
            } catch (Exception ex) {
                ex.printStackTrace();
                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
            }
        }
        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
    }

    /**
     * test d'existence et ajout de ressource
     *
     * @param resource :ressource a ajouter
     * @return LabelConstant : de statut d'existence
     */
    @Override
    public String addResource(ResourceWebApp resource) {
        return addResource(resource, false);
    }

    /**
     * SESSION DEMO test d'existence et ajout de ressource
     *
     * @param resource :ressource a ajouter
     * @return LabelConstant : de statut d'existence
     */
    @Override
    public String addResourceDemo(ResourceWebApp resource) {
        String kgDemo = "KG" + RequestContextHolder.currentRequestAttributes().getSessionId();
        if (Objects.nonNull(resource.getTargetKG()) && resource.getTargetKG().equals(kgDemo)) {
            return addResource(resource, true);
        }
        return messageSource.resolveCode("RESSOURCE_CREATION_HAS_NOT_PERMISSION",
                LabelConstant.RESSOURCE_CREATION_HAS_NOT_PERMISSION);
    }

    /**
     * sauvegarde de l'Url d'une ressource dans un GraphHolder
     *
     * @param label label de la ressource
     * @param url a sauvegarder
     * @param kgLabel label du graphe
     * @return LabelConstant
     */
    @Override
    public String saveUrl(String label, String url, String description) {
        try {
            resourceDao.saveRessourceUrl(label, url, description, 0L);
            return messageSource.resolveCode("URL_SUCCESSFULLY_SAVED", LabelConstant.URL_SUCCESSFULLY_SAVED);
        } catch (IOException e) {
            e.printStackTrace();
            return messageSource.resolveCode("URL_CREATION_ERROR", LabelConstant.URL_CREATION_ERROR);
        }
    }

    @Override
    public String saveEvent(String label, Float valueX, Float valueY, String sDateDebut, String sDateFin) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date dateDebut = null;
        Date dateFin = null;
        if (!"".equals(sDateDebut)) {
            try {
                dateDebut = sdf.parse(sDateDebut);
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(ResourceSAImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!"".equals(sDateFin)) {
            try {
                dateFin = sdf.parse(sDateFin);
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(ResourceSAImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            resourceDao.saveResourceEvent(label, valueX, valueY, dateDebut, dateFin);
            return messageSource.resolveCode("RESOURCE_EVENT_SUCCESSFULLY_CREATED",
                    LabelConstant.RESOURCE_EVENT_SUCCESSFULLY_CREATED);
        } catch (IOException ex) {
            ex.printStackTrace();
            return messageSource.resolveCode("RESOURCE_EVENT_SUCCESSFULLY_ERROR",
                    LabelConstant.RESOURCE_EVENT_SUCCESSFULLY_ERROR);
        }

    }

    @Override
    public String saveUserGroup(String label, List<String> agents, String userGroupType, String targetKg, String emitter, String action) {
        try {
            ResourceEntity test = resourceDao.getResourceByLabel(label);
            if ("CREATE".equals(action)) {
                resourceDao.saveResourceUserGroup(label, userGroupType);
                for (String agent : agents) {
                    this.createViewpointDate(label, agent, userGroupType, targetKg, emitter);
                }
            } else {
                resourceDao.saveResourceUserGroup(label, userGroupType);
                GraphEntity currentKg = graphDao.getGraphByName(targetKg);
                List<ViewpointEntity> neighbours = viewpointDao.getViewpointListByResourceAndGraph(test, currentKg);
                for (ViewpointEntity neighbour : neighbours) {
                    Boolean testIn = false;
                    for (String agent : agents) {
                        if (neighbour.getResource2().getLabel().equals(agent)) {
                            testIn = true;
                            break;
                        }
                    }
                    if (!testIn) {
                        userKgViewpointDAO.deleteByViewpoint(neighbour);
                        viewpointDao.deleteViewpoint(neighbour);
                    }
                }
                neighbours = viewpointDao.getViewpointListByResourceAndGraph(test, currentKg);
                for (String agent : agents) {
                    Boolean testIn = false;
                    for (ViewpointEntity neighbour : neighbours) {
                        if (neighbour.getResource2().getLabel().equals(agent)) {
                            testIn = true;
                            break;
                        }
                    }
                    if (!testIn) {
                        this.createViewpointDate(label, agent, userGroupType, targetKg, emitter);
                    }
                }

            }
            return messageSource.resolveCode("RESOURCE_EVENT_SUCCESSFULLY_CREATED",
                    LabelConstant.RESOURCE_EVENT_SUCCESSFULLY_CREATED);
        } catch (IOException ex) {
            ex.printStackTrace();
            return messageSource.resolveCode("RESOURCE_EVENT_SUCCESSFULLY_ERROR",
                    LabelConstant.RESOURCE_EVENT_SUCCESSFULLY_ERROR);
        }
    }

    private void createViewpointDate(String label, String agent, String userGroupType, String targetKg, String emitter) {
        ViewpointWebApp viewpoint = new ViewpointWebApp();
        viewpoint.setEmitter(emitter);
        viewpoint.setResource1(label);
        viewpoint.setResource2(agent);
        viewpoint.setTargetKG(targetKg);
        if (userGroupType.equals(UserGroupTypeConstant.FACTUAL)) {
            viewpoint.setType(ViewpointTypeConstant.FACTUAL);
            viewpointSA.addViewPoint(viewpoint, new Date());
        } else {
            //viewpoint type subjective
            viewpoint.setType(ViewpointTypeConstant.SUBJECTIVE);
            viewpointSA.addViewPoint(viewpoint, new Date());
        }
    }

    /**
     * retourne un Url
     *
     * @param NumDoc : URl
     * @return resource : ressource d'une Url
     */
    @Override
    public String setURL(NumericDocumentWebApp NumDoc) {
//        String res = "";
//        List<GraphEntity> graphs = graphDao.getGraphList();
//        for (GraphEntity graph : graphs) {
//            NumDoc.setTargetKG(graph.getName());
//            res += resourceRepository.setKernelURL(NumDoc);
//        }
//        return StringUtils.isEmpty(res) ? ""
//                : messageSource.resolveCode("KERNEL_URL_NOT_NUMERIC_DOCUMENT",
//                        LabelConstant.KERNEL_URL_NOT_NUMERIC_DOCUMENT);
        return "";
    }

    @Override
    public String getURL(String label, String targetKG) {
//        return resourceRepository.getURL(label, targetKG);
        return "";
    }

    /**
     * retourne une liste de classe des resources existant
     *
     * @return classList : liste des classes
     * @throws ClassNotFoundException : erreur montrant un manque de dependance
     */
    @Override
    public ArrayList<String> getResourceList() throws ClassNotFoundException {
        ArrayList<String> classList = new ArrayList<String>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(ResourceEntity.class));

        Set<BeanDefinition> components = provider.findCandidateComponents(LabelConstant.CANDIDATE_COMPONENTS);
        for (BeanDefinition component : components) {
            Class<?> cls = Class.forName(component.getBeanClassName());
            classList.add(cls.getCanonicalName());
        }
        Collections.sort(classList);
        return classList;
    }

    /**
     * retourne l'Url de la ressource precedante resource
     *
     * @param label : label de la ressource
     * @param kgNames
     * @param kgLabel : label du graphe
     * @return LabelConstant
     */
    @Override
    public String getResourcePreview(String label, List<String> kgNames, String kgLabel) {
        ResourceEntity r = resourceDao.getResourceByLabel(label);
        if (r == null) {
            return LabelConstant.BLANK_PAGE_URL;
        } else {
            if (StringUtils.isEmpty(r.getPreviewUrl())) {
                return LabelConstant.BLANK_PAGE_URL;
            } else {
                if (StringUtils.contains(r.getPreviewUrl(), "/upload?file=")) {
                    return servletContext.getContextPath()
                            + r.getPreviewUrl().substring(r.getPreviewUrl().indexOf("/upload?file="));
                }
                return r.getPreviewUrl();
            }
        }
    }

    /**
     * retourne le type classe de resource
     *
     * @param name nome de la ressource
     * @param kgLabel label du graphe
     * @return ResourceType
     */
    @Override
    public String getResourceType(String name, String kgLabel) {
        ResourceEntity r = resourceDao.getResourceByLabel(name);
        if (r == null) {
            return null;
        }
        return r.getType().getViewname();
    }

    @Override
    public Map<String, String> getResourceTypeList() {
        Map<String, String> temp = new LinkedHashMap<>();
        List<ResourceTypeEntity> types = resourceTypeDao.getTypeListOrderByViewname();
        for (ResourceTypeEntity type : types) {
            if (type.getName().equals("PhysicalDocument")) {
                continue;
            }
            if (type.getLabelEntity() != null && StringUtils.isNotEmpty(type.getLabelEntity().getMessageKey())) {
                temp.put(type.getName(), messageSource.resolveCode(type.getLabelEntity().getMessageKey(), type.getViewname()));
            } else {
                temp.put(type.getName(), type.getViewname());
            }
        }
        return temp;
    }

    @Override
    public Map<String, String> getResourceTypeColor() {
        Map<String, String> temp = new LinkedHashMap<>();
        List<ResourceTypeEntity> types = resourceTypeDao.getTypeListOrderByViewname();
        for (ResourceTypeEntity type : types) {
            temp.put(type.getName(), StringUtils.isEmpty(type.getColor()) ? "#617db4" : type.getColor());
        }
        return temp;
    }

    @Override
    public ResourceEntity getResourceByName(String name, String kgLabel) {
        return resourceDao.getResourceByLabel(name);
    }

    @Override
    public Boolean isUser(ResourceEntity r) {
        ResourceEntity resourceEntity = resourceDao.getResourceByLabel(r.getLabel());
        if (resourceEntity != null && resourceEntity instanceof UserEntity) {
            return (UserEntity) resourceEntity != null;
        }
        return false;
    }

    @Override
    public PreviewDto getPreview(String resource, List<String> kgNames, String kgLabel) {

        String currentUsername = "";
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            currentUsername = "Démo user";
        } else {
            currentUsername = authentication.getName();
        }

        GraphEntity currentkg = graphDao.getGraphByName(kgLabel);
        if (currentkg != null) {
            ResourceEntity rss = resourceDao.getResourceByLabel(resource);
            if (rss != null) {
                PreviewDto dto = new PreviewDto();
                dto.setId(rss.getId().intValue());
                dto.setLabel(rss.getLabel());
                dto.setDescription(rss.getDescription());
                dto.setType(rss.getType().getName());
                dto.setCanSetPreview(false);
                if (currentUsername.equals(resource)) {
                    dto.setCanSetPreview(true);
                }
                dto.setCanUpdateRessource(false);
                if (currentUsername.equals("Démo user") || currentUsername.equals("anonymousUser")) {
                    dto.setCanCreateFactualViewpoint(true);
                    dto.setCanCreateSubjectiveViewpoint(true);
                    dto.setCanCreateSemanticViewpoint(true);
                    dto.setCanSetPreview(true);
                } else {
                    UserEntity user = resourceDao.getUserEntityByLabel(currentUsername);
                    dto.setCanCreateFactualViewpoint(false);
                    dto.setCanCreateSubjectiveViewpoint(false);
                    dto.setCanCreateSemanticViewpoint(false);
                    if (Objects.nonNull(user)) {
                        if (StringUtils.equals(rss.getCreator().getLabel(), user.getLabel())) {
                            dto.setCanUpdateRessource(true);
                        }
                        Map<String, String> userUserActions = userActionSA.getCodeMenu(user);
                        if (userUserActions.keySet().contains("VWP_ADD_FTL")) {
                            dto.setCanCreateFactualViewpoint(true);
                        }
                        if (userUserActions.keySet().contains("VWP_ADD_SBJ")) {
                            dto.setCanCreateSubjectiveViewpoint(true);
                        }
                        if (userUserActions.keySet().contains("VWP_ADD_STC")) {
                            dto.setCanCreateSemanticViewpoint(true);
                        }
                    }
                }
                if (rss instanceof EventEntity) {
                    ResourceEntity entity = resourceDao.getResourceByLabel(rss.getLabel());
                    if (entity != null && entity instanceof EventEntity) {
                        EventEntity event = (EventEntity) entity;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        dto.setX(event.getX());
                        dto.setY(event.getY());
                        dto.setDateDebut(event.getStartDate() == null ? "" : sdf.format(event.getStartDate()));
                        dto.setDateFin(event.getEndDate() == null ? "" : sdf.format(event.getEndDate()));
                    }
                }
                if (rss instanceof UserGroupEntity) {
                    ResourceEntity entity = resourceDao.getResourceByLabel(rss.getLabel());
                    if (entity != null && entity instanceof UserGroupEntity) {
                        UserGroupEntity userGroupEntity = (UserGroupEntity) entity;
                        if (!Objects.isNull(userGroupEntity.getUserGroupType())) {
                            dto.setUserGroupType(userGroupEntity.getUserGroupType().getName());
                        }
                        ResourceEntity search = resourceDao.getResourceByLabel(resource);
                        if (search != null) {
                            List<ResourceEntity> neighbours = this.retrieveUserFromGraphAndUserGroup(currentkg, search);
                            List<String> neighboursString = neighbours
                                    .stream()
                                    .map(ResourceEntity::getLabel)
                                    .collect(Collectors.toCollection(ArrayList::new));
                            dto.setNeighboursAgent(neighboursString);
                        }
                    }
                }

                ResourceEntity user = resourceDao.getResourceByLabel(currentUsername);
                ResourceEntity search = resourceDao.getResourceByLabel(resource);
                if (search != null) {
                    List<ViewpointEntity> userPanel = new ArrayList<>();
                    List<ViewpointEntity> otherPanel = new ArrayList<>();

                    List<ViewpointEntity> neighbours = viewpointSA.getViewpointsByGraphAndResourceWithVisibility(search, currentkg, user);

                    for (ViewpointEntity connectedViewpoint : neighbours) {
                        if (connectedViewpoint.getType().equals(new ViewpointTypeEntity("FactualViewpoint"))) {
                            if (connectedViewpoint.getResource1().getLabel().equals(currentUsername) || connectedViewpoint.getResource2().getLabel().equals(currentUsername)) {
                                dto.setCanSetPreview(true);
                            }
                        }
                        if (connectedViewpoint.getType().equals(new ViewpointTypeEntity("PreviewViewpoint"))) {
                            continue;
                        }

                        if (user == null) {
                            otherPanel.add(connectedViewpoint);
                        } else if (connectedViewpoint.getEmitter().equals(user)) {
                            userPanel.add(connectedViewpoint);
                        } else {
                            otherPanel.add(connectedViewpoint);
                        }
                    }
                    Collections.sort(userPanel, new Comparator<ViewpointEntity>() {
                        @Override
                        public int compare(ViewpointEntity o1, ViewpointEntity o2) {
                            return o2.getCreationDate().compareTo(o1.getCreationDate());
                        }
                    });
                    Collections.sort(otherPanel, new Comparator<ViewpointEntity>() {
                        @Override
                        public int compare(ViewpointEntity o1, ViewpointEntity o2) {
                            return o2.getCreationDate().compareTo(o1.getCreationDate());
                        }
                    });
                    if (!userPanel.isEmpty()) {
                        ViewpointEntity cv = userPanel.get(0);
                        ResourceEntity toAdd = cv.getResource1().getLabel().equals(resource) ? cv.getResource2() : cv.getResource1();
                        if (toAdd instanceof NumericDocumentEntity) {
                            dto.setUrl(((NumericDocumentEntity) toAdd).getUrl());
                        }
                    } else if (!otherPanel.isEmpty()) {
                        ViewpointEntity cv = otherPanel.get(0);
                        ResourceEntity toAdd = cv.getResource1().getLabel().equals(resource) ? cv.getResource2() : cv.getResource1();
                        if (toAdd instanceof NumericDocumentEntity) {
                            dto.setUrl(((NumericDocumentEntity) toAdd).getUrl());
                        }
                    }
                    if (search instanceof NumericDocumentEntity) {
                        dto.setResourceUrl(((NumericDocumentEntity) search).getUrl());
                    } else {
                        dto.setResourceUrl(search.getPreviewUrl());
                    }
                    dto.setPreviewUrl(search.getPreviewUrl());
                }
                return dto;
            }
        }

        return null;
    }

    @Override
    public Map<Long, String> getAllResources(String kgLabel) {
        Map<Long, String> output = new HashMap<>();
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new HashMap<>();
        }
        List<ExternalFolder> foldersByUser = userFolderDAO.getFoldersByUser(user);
        List<ResourceScanDTO> allResources = this.getResourceList("", "", "", "", "", kgLabel);
        for (ResourceScanDTO res : allResources) {
            ResourceEntity resource = resourceDao.getResourceById(res.getId());
            if (resource instanceof NumericDocumentEntity) {
                NumericDocumentEntity document = (NumericDocumentEntity) resource;
                if (Objects.nonNull(document.getFolder())) {
                    if (!foldersByUser.contains(document.getFolder())) {
                        output.put(resource.getId(), resource.getLabel());
                    } else {
                        continue;
                    }
                }
            } else {
                output.put(resource.getId(), resource.getLabel());
            }
        }
        return output;
    }

    @Override
    public Map<Long, String> getResourcesByIsUser(String kgLabel, Boolean isUser) {
        if (isUser) {
            return this.getAllUsers(kgLabel);
        } else {
            return this.getAllResources(kgLabel);
        }
    }

    @Override
    public Map<Long, String> getAllUsers(String kgLabel) {
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new HashMap<>();
        }
        List<ResourceEntity> allResources = resourceDao.getALLUsers();
        return allResources
                .stream()
                .collect(Collectors.toMap(ResourceEntity::getId, ResourceEntity::getLabel));
    }

    @Override
    public Map<Long, String> getAllTopics(String kgLabel) {
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new HashMap<>();
        }
        List<ResourceEntity> allTopics = resourceDao.getAllTopics();
        return allTopics
                .stream()
                .collect(Collectors.toMap(ResourceEntity::getId, ResourceEntity::getLabel));
    }

    @Override
    public Map<Long, String> getAllUserGroups(String kgLabel) {
        GraphEntity graph = graphDao.getGraphByName(kgLabel);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(graph) || Objects.isNull(user)) {
            return new HashMap<>();
        }

        List<ResourceEntity> allResources = resourceDao.getAllUserGroups();
        return allResources
                .stream()
                .collect(Collectors.toMap(ResourceEntity::getId, ResourceEntity::getLabel));
    }

    @Override
    public Map<Long, String> getResourcesByGroup(Long igGroup) {
        Map<Long, String> output = new HashMap<>();
        RGroupEntity rGroupEntity = rGroupRepository.getRGroupById(igGroup);
        if (rGroupEntity == null) {
            return output;
        }
        List<ResourceEntity> resources = resourceDao.getResourcesByGroup(rGroupEntity);
        return resources
                .stream()
                .collect(Collectors.toMap(ResourceEntity::getId, ResourceEntity::getLabel));
    }

    @Override
    public List<ResourceEntity> getResourcesKernelByGroup(Long group, String kgLabel) {
        List<ResourceEntity> resources = new ArrayList<>();
        Map<Long, String> resourcesByGroup = getResourcesByGroup(group);
        if (resourcesByGroup.size() == 0 || resourcesByGroup == null) {
            return resources;
        }
        for (Map.Entry<Long, String> entry : resourcesByGroup.entrySet()) {
            String value = entry.getValue();
            resources.add(getResourceByName(value, kgLabel));
        }
        return resources;
    }

    @Override
    public List<ResourceEntity> getResourcesKernelByList(List<String> labelList, String kgLabel) {
        List<ResourceEntity> resources = new ArrayList<>();
        for (String label : labelList) {
            resources.add(getResourceByName(label, kgLabel));
        }
        return resources;
    }

    @Override
    public Map<Long, String> getFolderFromLiferay() {
        ObjectMapper mapper = new ObjectMapper();
        String url = liferayPortalUrl + "/api/jsonws/foo.numdocfolder/get-folder-list";
        String output = "";
        Map<Long, String> folderOutputMap = new HashMap<>();
        List<FolderDto> folderDtoList = new ArrayList<FolderDto>();
        try {
            output = GetWs.doGet(url, liferayPortalUserLogin, liferayPortalUserPassword);
            System.out.println("output:" + output);
            folderDtoList = Arrays.asList(mapper.readValue(output, FolderDto[].class
            ));
            for (FolderDto folderDto : folderDtoList) {
                folderOutputMap.put(folderDto.getId(), folderDto.getFolderName());
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ResourceSAImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return folderOutputMap;
    }

    @Override
    public Map<Long, String> getFolderFromLiferay(String username) {
        ObjectMapper mapper = new ObjectMapper();
        String url = liferayPortalUrl + "/api/jsonws/foo.numdocfolder/get-folder-list-by-user?username=" + username;
        String output = "";
        Map<Long, String> folderOutputMap = new HashMap<>();
        List<FolderDto> folderDtoList = new ArrayList<FolderDto>();
        try {
            output = GetWs.doGet(url, liferayPortalUserLogin, liferayPortalUserPassword);
            System.out.println("output:" + output);
            folderDtoList = Arrays.asList(mapper.readValue(output, FolderDto[].class
            ));
            for (FolderDto folderDto : folderDtoList) {
                folderOutputMap.put(folderDto.getId(), folderDto.getFolderName());
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ResourceSAImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return folderOutputMap;
    }

    public String addResourceFromNextcloud(String kgLabel, String emitterString, List<NextNumericDocumentDTO> nextNumericDocumentDTOs) {
        String siVwaPrefix = "SI_VWA_NEXTCLOUD_";
        String siNextCloudPrefix = "SI_NEXTCLOUD_";
        ResourceEntity emitter = resourceDao.getResourceByLabel(emitterString);

        if (Objects.isNull(emitter)) {
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
        }

        if (!nextNumericDocumentDTOs.isEmpty()) {
            for (NextNumericDocumentDTO nextNumericDocumentDTO : nextNumericDocumentDTOs) {
                ExternalFolder externalFolder = externalFolderDAO.findByLabel(nextNumericDocumentDTO.getSi());
                if (externalFolder != null) {
                    if (!externalFolder.getCodeLdap().startsWith(siNextCloudPrefix)) {
                        continue;
                    }
                    String siSuffix = externalFolder.getCodeLdap().replace(siNextCloudPrefix, "");
                    if (siSuffix.contains("_")) {
                        siSuffix = siSuffix.split("_")[0];
                    }
                    if (StringUtils.isEmpty(siSuffix)) {
                        continue;
                    }
                    kgLabel = siVwaPrefix + siSuffix;
                    if (!graphDao.existGraphByName(kgLabel)) {
                        continue;
                    }
                } else {
                    continue;
                }

                ViewpointWebApp vw = new ViewpointWebApp();
                ViewpointEntity checkViewpointExist;
                String output = "";

                ResourceEntity researchDoc = resourceDao.getResourceByLabel(nextNumericDocumentDTO.getLabel());
                ResourceEntity researchExternalID = resourceDao.getResourceByExternalId(nextNumericDocumentDTO.getId());
                if(researchExternalID != null) {
                    researchDoc = researchExternalID;
                }

                if (Objects.isNull(researchDoc)) {
                    ResourceEntity isNumericDocumentExist = resourceDao.getResourceByExternalId(nextNumericDocumentDTO.getId());
                    if (!Objects.isNull(isNumericDocumentExist)) {
                        isNumericDocumentExist.setLabel(nextNumericDocumentDTO.getLabel());
                        if (nextNumericDocumentDTO.getDescription() != null && nextNumericDocumentDTO.getDescription().length() != 0) {
                            isNumericDocumentExist.setDescription(nextNumericDocumentDTO.getDescription());
                        }
                        if (nextNumericDocumentDTO.getLink() != null && nextNumericDocumentDTO.getLink().length() != 0) {
                            isNumericDocumentExist.setPreviewUrl(nextNumericDocumentDTO.getLink());
                        }
                        resourceDao.saveResource(isNumericDocumentExist);
                    } else {
                        ResourceWebApp rwDoc = new ResourceWebApp();
                        rwDoc.setAction("CREATE");
                        rwDoc.setLabel(nextNumericDocumentDTO.getLabel());
                        rwDoc.setDescription(nextNumericDocumentDTO.getDescription());
                        if (nextNumericDocumentDTO.getLink() != null && nextNumericDocumentDTO.getLink().length() != 0) {
                            rwDoc.setPreviewUrl(nextNumericDocumentDTO.getLink());
                        } else {
                            rwDoc.setPreviewUrl(nextNumericDocumentDTO.getUrl());
                        }
                        rwDoc.setType("com.viewpoints.kernel.knowledgeGraph.nodes.superModel.resources.NumericDocument");
                        rwDoc.setTargetKG(kgLabel);
                        rwDoc.setEmitter(emitterString);
                        rwDoc.setFolder(nextNumericDocumentDTO.getSi());
                        String outputrwDoc = addResource(rwDoc);
                        if (outputrwDoc.equals(messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED))) {
                            outputrwDoc = addNextDocResource(nextNumericDocumentDTO);
                            if (outputrwDoc.equals(messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR))) {
                                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
                            }
                        }
                    }
                } else {
                    if (researchDoc instanceof NumericDocumentEntity && Objects.isNull(researchDoc.getExternalId())) {
                        researchDoc.setExternalId(nextNumericDocumentDTO.getId());
                    }
                    if(!StringUtils.equals(researchDoc.getLabel(), nextNumericDocumentDTO.getLabel())) {
                        researchDoc.setLabel(nextNumericDocumentDTO.getLabel());
                    }
                    if (!Objects.equals(researchDoc.getExternalId(), nextNumericDocumentDTO.getId())) {

                        String labelLike = nextNumericDocumentDTO.getLabel().split(Pattern.quote("."))[0];
                        List<String> listResource = resourceDao.getAllLabelEquals(labelLike);
                        String newNameLabel = this.autoIncrementLabelResource(listResource, nextNumericDocumentDTO.getLabel());

                        ResourceWebApp rwDoc = new ResourceWebApp();
                        rwDoc.setAction("CREATE");
                        rwDoc.setLabel(newNameLabel);
                        rwDoc.setDescription("");
                        rwDoc.setResourceUrl(nextNumericDocumentDTO.getUrl());
                        rwDoc.setType("com.viewpoints.kernel.knowledgeGraph.nodes.superModel.resources.NumericDocument");
                        rwDoc.setTargetKG(kgLabel);
                        rwDoc.setEmitter(emitterString);
                        rwDoc.setFolder(nextNumericDocumentDTO.getSi());
                        String outputrwDoc = addResource(rwDoc);
                        if (outputrwDoc.equals(messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED))) {
                            nextNumericDocumentDTO.setLabel(newNameLabel);
                            outputrwDoc = addNextDocResource(nextNumericDocumentDTO);
                            if (outputrwDoc.equals(messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR))) {
                                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
                            }
                        }
                        if (output.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
                        }
                    } else {
                        if (researchDoc instanceof NumericDocumentEntity) {
                            NumericDocumentEntity nde = (NumericDocumentEntity) researchDoc;
                            if (nextNumericDocumentDTO.getDescription() != null && nextNumericDocumentDTO.getDescription().length() != 0) {
                                nde.setDescription(nextNumericDocumentDTO.getDescription());
                            }
                            if(nde.getPreviewUrl() == null){
                                if (nextNumericDocumentDTO.getLink() != null && nextNumericDocumentDTO.getLink().length() != 0) {
                                    nde.setPreviewUrl(nextNumericDocumentDTO.getLink());
                                } else {
                                    nde.setPreviewUrl(nextNumericDocumentDTO.getUrl());
                                }
                            }
                            if (nextNumericDocumentDTO.getUrl() != null && nextNumericDocumentDTO.getUrl().length() != 0) {
                                nde.setUrl(nextNumericDocumentDTO.getUrl());
                            }
                            nde.setFolder(externalFolder);

                            resourceDao.saveResource(nde);
                        }
                    }
                }

                vw.setResource1(nextNumericDocumentDTO.getLabel());

                if (CollectionUtils.isNotEmpty(nextNumericDocumentDTO.getAuthors())) {
                    for (String author : nextNumericDocumentDTO.getAuthors()) {
                        if (StringUtils.isNotEmpty(author) && !"undefined".equals(author)) {
                            ResourceEntity researchCreator = resourceDao.getResourceByLabel(author);
                            if (researchCreator == null) {
                                ResourceWebApp rwCreator = new ResourceWebApp();
                                rwCreator.setLabel(author);
                                rwCreator.setTargetKG(kgLabel);
                                rwCreator.setAction("CREATE");
                                rwCreator.setEmitter(emitterString);
                                rwCreator.setType("com.viewpoints.kernel.knowledgeGraph.nodes.superModel.resources.Topic");
                                output = addResource(rwCreator);
                                if (output.equals(messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR))) {
                                    return output;
                                }
                            } else {
                                researchDoc = resourceDao.getResourceByLabel(nextNumericDocumentDTO.getLabel());
                                checkViewpointExist = viewpointDao.getViewpointByResource1andResource2(researchDoc, researchCreator);
                                if (checkViewpointExist != null) {
                                    if ((researchCreator instanceof UserEntity && checkViewpointExist.getType().getName().equals("DynamicCreateViewpoint"))
                                            || (!(researchCreator instanceof UserEntity) && checkViewpointExist.getType().getName().equals("FactualViewpoint"))) {
                                        continue;
                                    }
                                }
                            }

                            vw.setTargetKG(kgLabel);
                            vw.setResource2(author);
                            if (researchCreator != null) {
                                if (researchCreator instanceof UserEntity) {
                                    vw.setEmitter(author);
                                    vw.setType(ViewpointTypeConstant.DYNAMIC_CREATE);
                                } else {
                                    vw.setEmitter(emitterString);
                                    vw.setType(ViewpointTypeConstant.FACTUAL);
                                }
                            } else {
                                vw.setEmitter(emitterString);
                                vw.setType(ViewpointTypeConstant.FACTUAL);
                            }

                            output = viewpointSA.addViewPoint(vw);
                            if (output.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR + " authors");
                            }
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(nextNumericDocumentDTO.getTags())) {
                    for (String tag : nextNumericDocumentDTO.getTags()) {
                        if (StringUtils.isNotEmpty(tag) && !"VWA".equals(tag)) {
                            ResourceEntity researchTag = resourceDao.getResourceByLabel(tag);
                            if (researchTag == null) {
                                ResourceWebApp rwDescriptor = new ResourceWebApp();
                                rwDescriptor.setAction("CREATE");
                                rwDescriptor.setLabel(tag);
                                rwDescriptor.setTargetKG(kgLabel);
                                rwDescriptor.setEmitter(emitterString);
                                rwDescriptor.setType("com.viewpoints.kernel.knowledgeGraph.nodes.superModel.resources.Topic");
                                output = addResource(rwDescriptor);
                                if (output.equals(messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR))) {
                                    return output + " failed dans tag";
                                }
                            } else {
                                researchDoc = resourceDao.getResourceByLabel(nextNumericDocumentDTO.getLabel());
                                checkViewpointExist = viewpointDao.getViewpointByResource1andResource2(researchDoc, researchTag);
                                if (checkViewpointExist != null) {
                                    continue;
                                }
                            }
                            vw.setTargetKG(kgLabel);
                            vw.setEmitter(emitterString);
                            vw.setResource2(tag);
                            vw.setType(ViewpointTypeConstant.SUBJECTIVE);
                            output = viewpointSA.addViewPoint(vw);
                            if (output.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR + " erreur dans tag");
                            }
                        }
                    }
                }

            }
        }

//        Optional<HistorySync> historySyncOptional = historySyncDao.findByUserId(emitter.getId());
//        HistorySync historySync = null;
//
//        if (!historySyncOptional.isPresent()) {
//            historySync = new HistorySync();
//            historySync.setUserId(emitter.getId());
//        } else {
//            HistorySync history = historySyncOptional.get();
//            historySync = new HistorySync();
//            historySync.setId(history.getId());
//            historySync.setUserId(history.getUserId());
//        }
//
//        historySync.setLastSyncDate(new Date());
//        historySyncDao.createOrUpdate(historySync);

        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
    }

    public List<NextNumericDocumentDTO> buildNextNumericDocumentDTOs(Long activityId,Long externalId ) {
        List<ExternalFolder> folders = externalFolderDAO.findAllByExternalId(externalId);
        Activity activity = activityDAO.findById(activityId).orElseThrow(() -> new RuntimeException("Activity not found"));

        List<NextNumericDocumentDTO> dtos = new ArrayList<>();
        for (ExternalFolder folder : folders) {
            NextNumericDocumentDTO dto = new NextNumericDocumentDTO();
            dto.setLabel(folder.getLabel());
            dto.setUrl(folder.getPath());
            dto.setSi(folder.getCodeLdap());
            dto.setDescription(folder.getDescription());
            dto.setLink(folder.getPath());
            dto.setAuthors(customPropertyDAO.findAllById(folder.getId()).stream()
                    .map(AuthorInfo::getPropertyName).collect(Collectors.toList()));
            dto.setTags(systemTagDAO.findAllById( folder.getId()).stream()
                    .map(Tag::getDisplayName).collect(Collectors.toList()));
            dto.setActivity(activity);
            dtos.add(dto);
        }
        return dtos;
    }

    private String autoIncrementLabelResource(List<String> listResource, String resourceLabel) {

        int max = 0;
        String finalLabel = resourceLabel;
        if (!listResource.isEmpty()) {
            String requiredString = "";
            for (String name : listResource) {
                try {
                    requiredString = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                    try {
                        int number = Integer.valueOf(requiredString);
                        if (number > max) {
                            max = number;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        String[] nomCut = finalLabel.split(Pattern.quote("."));
        int size = nomCut.length;
        String ext = "";
        String namePartial = "";

        if (size > 1) {
            ext = "." + nomCut[size - 1];
            for (int i = 0; i < size - 1; i++) {
                if (i > 0) {
                    namePartial = namePartial + ".";
                }

                namePartial = namePartial + nomCut[i];
            }
            finalLabel = namePartial + " (" + (max + 1) + ")" + ext;
        } else {
            finalLabel = finalLabel + " (" + max + ")";
        }

        return finalLabel;
    }

    public void addLosenViewpoint() {

    }

    private String addResourceFromLiferay(String kgLabel, List<NumericDocumentDto> resourceLiferayDtoList) {

        for (NumericDocumentDto resourceLiferayDto : resourceLiferayDtoList) {
            ResourceEntity researchCreator = resourceDao.getResourceByLabel(resourceLiferayDto.getCreator());
            if (researchCreator == null) {
                ResourceWebApp rwCreator = new ResourceWebApp();
                rwCreator.setLabel(resourceLiferayDto.getCreator());
                rwCreator.setTargetKG(kgLabel);
                rwCreator.setType("com.viewpoints.kernel.knowledgeGraph.nodes.superModel.resources.User");
                addResource(rwCreator);
            }

            ResourceEntity research = resourceDao.getResourceByLabel(resourceLiferayDto.getLabel());
            List<GraphEntity> graphs = graphDao.getGraphList();
            if (research == null) {
                String output = addExternalResource(resourceLiferayDto, graphs, kgLabel);
                if (output.equals(
                        messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR))) {
                    return output;
                }
            }

            ViewpointTypeEntity viewpointTypeEntity = viewpointTypeDao
                    .getViewpointTypeEntityByName("FactualViewpoint");
            ResourceEntity resourceEntity1 = resourceDao
                    .getResourceByLabel(resourceLiferayDto.getCreator());
            ResourceEntity resourceEntity2 = resourceDao.getResourceByLabel(resourceLiferayDto.getLabel());
            ResourceEntity emitterEntity = resourceDao
                    .getResourceByLabel(resourceLiferayDto.getCreator());
            GraphEntity graphEnity = graphDao.getGraphByName(kgLabel);
            ViewpointEntity viewpointEntity = viewpointDao.getViewpointFromResourceLiferay(viewpointTypeEntity,
                    resourceEntity1, resourceEntity2, emitterEntity, graphEnity);
            if (viewpointEntity == null) {
                viewpointSA.addViewpointFromResourceLiferay(resourceLiferayDto, kgLabel);
            }

            if (resourceLiferayDto.getTags() != "") {

                if (resourceLiferayDto.getTags().split(",").length > 0) {
                    String[] tagTab = resourceLiferayDto.getTags().split(",");
                    int tagTabLength = tagTab.length;
                    for (int i = 0; i < tagTabLength; i++) {

                        DescriptorDto descriptor = new DescriptorDto();
                        descriptor.setCreator(resourceLiferayDto.getCreator());
                        descriptor.setLabel(tagTab[i]);
                        descriptor.setType("Descriptor");
                        descriptor.setNumDocLabel(resourceLiferayDto.getLabel());

                        ResourceEntity descriptorKernel = resourceDao.getResourceByLabel(descriptor.getLabel());

                        if (descriptorKernel == null) {

                            String output = addExternalResource(descriptor, graphs, kgLabel);
                            if (output.equals(messageSource.resolveCode("RESOURCE_CREATION_ERROR",
                                    LabelConstant.RESOURCE_CREATION_ERROR))) {
                                return output;
                            }
                        }
                        viewpointTypeEntity = viewpointTypeDao
                                .getViewpointTypeEntityByName("SubjectiveViewpoint");
                        resourceEntity1 = resourceDao
                                .getResourceByLabel(descriptor.getNumDocLabel());
                        resourceEntity2 = resourceDao.getResourceByLabel(descriptor.getLabel());
                        emitterEntity = resourceDao
                                .getResourceByLabel(descriptor.getCreator());
                        graphEnity = graphDao.getGraphByName(kgLabel);
                        viewpointEntity = viewpointDao.getViewpointFromResourceLiferay(
                                viewpointTypeEntity, resourceEntity1, resourceEntity2, emitterEntity, graphEnity);

                        if (viewpointEntity == null) {
                            viewpointSA.addViewpointFromResourceLiferay(descriptor, kgLabel);
                        }
                    }
                }

            }
        }
        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
    }

    private String addExternalResource(ResourceLiferay resourceLiferay, List<GraphEntity> graphs, String kgLabel) {
        try {
            ResourceEntity resKernel = null;
//            for (GraphEntity graph : graphs) {
//                resKernel = resourceDao.saveResourceLiferay(resourceLiferay, graph.getName());
//            }
            if (resKernel != null) {
                ResourceEntity testEntity = resourceDao.getResourceByLabel(resKernel.getLabel());
//                if (testEntity == null) {
//                    resourceDao.saveResourceEntity(resourceLiferay);
//                }
            }
            for (GraphEntity graph : graphs) {
                if (resourceLiferay instanceof NumericDocumentDto) {
                    NumericDocumentDto numDocDto = (NumericDocumentDto) resourceLiferay;
                    resourceDao.saveRessourceUrl(resKernel.getLabel(), liferayPortalUrl + numDocDto.getUrl(), numDocDto.getDescription(), numDocDto.getId());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
        }
        return "OK";
    }

    private String addNextDocResource(NextNumericDocumentDTO nextNumericDocumentDTO) {
        try {
            resourceDao.saveNextDocProperties(nextNumericDocumentDTO);
        } catch (Exception ex) {
            ex.printStackTrace();
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR + " tanjona error " + ex);
        }
        return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
    }

    @Override
    public String getResourceFromLiferay(Long folderId, Long userId, String sDateFrom, String sDateTo, String kgLabel) {
        if (!sDateFrom.equals("")) {
            sDateFrom = formatDateForUrl(sDateFrom);
        }
        if (!sDateTo.equals("")) {
            sDateTo = formatDateForUrl(sDateTo);
        }
        String url = liferayPortalUrl + "/api/jsonws/foo.numdocfile/get-file-list?folderId=" + folderId + "&userId="
                + userId + "&sDateFrom=" + sDateFrom + "&sDateTo=" + sDateTo;
        String output = "";
        ObjectMapper mapper = new ObjectMapper();
        List<NumericDocumentDto> resourceLiferayDtoList = new ArrayList();
        try {
            output = GetWs.doGet(url, liferayPortalUserLogin, liferayPortalUserPassword);
            System.out.println("output:" + output);

            resourceLiferayDtoList = Arrays.asList(mapper.readValue(output, NumericDocumentDto[].class
            ));
            return addResourceFromLiferay(kgLabel, resourceLiferayDtoList);
        } catch (Exception ex) {
            ex.printStackTrace();
            return messageSource.resolveCode(url, LabelConstant.CONNEXION_ERROR);
        }
    }

    @Override
    public String getResourceFromLiferay(Long folderId, String username, String sDateFrom, String sDateTo, String kgLabel) {
        if (!sDateFrom.equals("")) {
            sDateFrom = formatDateForUrl(sDateFrom);
        }
        if (!sDateTo.equals("")) {
            sDateTo = formatDateForUrl(sDateTo);
        }
        String url = liferayPortalUrl + "/api/jsonws/foo.numdocfile/get-file-list-by-folder?folderId=" + folderId + "&username="
                + username + "&sDateFrom=" + sDateFrom + "&sDateTo=" + sDateTo;
        String output = "";
        ObjectMapper mapper = new ObjectMapper();
        List<NumericDocumentDto> resourceLiferayDtoList = new ArrayList();
        try {
            output = GetWs.doGet(url, liferayPortalUserLogin, liferayPortalUserPassword);
            System.out.println("output:" + output);

            resourceLiferayDtoList = Arrays.asList(mapper.readValue(output, NumericDocumentDto[].class
            ));
            return addResourceFromLiferay(kgLabel, resourceLiferayDtoList);
        } catch (Exception ex) {
            ex.printStackTrace();
            return messageSource.resolveCode(url, LabelConstant.CONNEXION_ERROR);
        }
    }

    @Override
    public String getResourceFromNextCloud(String kgLabel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String connectedUser = auth.getName();

        if (connectedUser == null) {
            LOGGER.warning("Le nom d'utilisateur est null");
            return "Erreur : Nom d'utilisateur introuvable";
        }

        List<NextNumericDocumentDTO> nextNumericDocumentDTOList = new ArrayList<>();

        try {
            List<FileDTO.Response> exteFolders = getFileDetails(connectedUser);
            nextNumericDocumentDTOList = processFolders(exteFolders, connectedUser, kgLabel);

            if (!nextNumericDocumentDTOList.isEmpty()) {
                return addResourceFromNextcloud(kgLabel, connectedUser, nextNumericDocumentDTOList);
            } else {
                return messageSource.resolveCode("RESOURCE_SUCCESSFULLY_CREATED", LabelConstant.RESOURCE_SUCCESSFULLY_CREATED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.severe("Erreur lors de la récupération de la ressource depuis NextCloud : " + ex.getMessage());
            return messageSource.resolveCode(LabelConstant.CONNEXION_ERROR + " " + ex + "  nextNumericDocumentDTO == " + nextNumericDocumentDTOList.toString());
        }
    }

    private String getLastSyncDate(String username) {
        UserEntity emitter = resourceDao.getUserByLabel(username);
        HistorySync historySync = historySyncDao.findById(emitter.getId());
        return (historySync == null) ? "1234" : formatDateForUrl(historySync.getLastSyncDate().toString());
    }

    private List<NextNumericDocumentDTO> processFolders(List<FileDTO.Response> exteFolders, String username, String kgLabel) throws IOException {
        List<NextNumericDocumentDTO> nextNumericDocumentDTOList = new ArrayList<>();
        UserEntity user = resourceDao.getUserByLabel(username);
        Long userId = user.getId();
        HistorySync historySync = historySyncDao.findById(userId);

        if (historySync == null) {
            historySync = new HistorySync();
            historySync.setUserId(userId);
            historySync.setLastUserActivityID(1L);
            historySyncDao.createOrUpdate(historySync);
        }

        List<ExternalFolder> efsToSync = syncAndFilterExternalFolderRacine(exteFolders);

        if(CollectionUtils.isEmpty(efsToSync)) {
            return nextNumericDocumentDTOList;
        }

        List<ActivitiesDTO.ActivityDataDTO> activities = getActivitiesForExternalFolder(historySync);
        if(CollectionUtils.isNotEmpty(activities)) {
            List<ActivitiesDTO.ActivityDataDTO> activitiesFiltered = filterNextCloudActivities(activities);
            activities = activitiesFiltered;
        }

        if(CollectionUtils.isNotEmpty(activities)) {
            List<String> folderRacinesNames = efsToSync.stream()
                    .map(ExternalFolder::getLabel)
                    .collect(Collectors.toList());
            for (ActivitiesDTO.ActivityDataDTO activity : activities) {
                NextNumericDocumentDTO dto = processActivityV2(activity, user, folderRacinesNames);
                if(dto != null) {
                    nextNumericDocumentDTOList.add(dto);
                }
            }
        }

        return nextNumericDocumentDTOList;
    }

    private List<ActivitiesDTO.ActivityDataDTO> filterNextCloudActivities(List<ActivitiesDTO.ActivityDataDTO> activities) {
        List<ActivitiesDTO.ActivityDataDTO> result = new ArrayList<>();
        List<ActivitiesDTO.ActivityDataDTO> tmp = new ArrayList<>();
        List<Integer> fileDeleted = new ArrayList<>();
        for (ActivitiesDTO.ActivityDataDTO activity : activities) {
            if (StringUtils.equals("comments", activity.getApp())) {
                continue;
            }
            if (StringUtils.equals("settings", activity.getApp())) {
                continue;
            }
            if (StringUtils.equals("systemtag", activity.getObjectType())) {
                continue;
            }
            if (StringUtils.equals("file_deleted", activity.getType())) {
                fileDeleted.add(activity.getObjectId());
            }
            tmp.add(activity);
        }
        for (ActivitiesDTO.ActivityDataDTO act : tmp) {
            if (fileDeleted.contains(act.getObjectId())) {
                continue;
            }
            result.add(act);
        }

        return result;
    }

    private List<ExternalFolder> syncAndFilterExternalFolderRacine(List<FileDTO.Response> nextCloudFolderRacines) {
        List<ExternalFolder> externalFolders = new ArrayList<>();

        for (FileDTO.Response nextCloudFolderRacine : nextCloudFolderRacines) {
            String nextCloudPath = nextCloudFolderRacine.getHref();
            if (!nextCloudPath.endsWith("/")) {
                continue;
            }
            // Récupérer la dernière chaîne de caractères entre les slashes
            String[] parts = nextCloudPath.split("/");
            String lastPart = parts[parts.length - 1];
            String nextCloudFolderName = "";
            try {
                // Décoder la chaîne de caractères
                nextCloudFolderName = URLDecoder.decode(lastPart, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException ex) {
                nextCloudFolderName = nextCloudFolderName.replace("%20", " ");
            }

            //Update ExternalFolder ExternalID
            ExternalFolder ef = externalFolderDAO.findByLabel(nextCloudFolderName);
            if (ef == null) {
                continue;
            } else {
                String nextCloudID = nextCloudFolderRacine.getPropstat().get(0).getProp().getFileid();
                ef.setExternalId(Long.parseLong(nextCloudID));
                externalFolderDAO.createOrUpdate(ef);

                //Prendre uniquement les dossiers racines cynchronalisable en VWA
                String siVwaPrefix = "SI_VWA_NEXTCLOUD_";
                String siNextCloudPrefix = "SI_NEXTCLOUD_";

                if (!ef.getCodeLdap().startsWith(siNextCloudPrefix)) {
                    continue;
                }
                String siSuffix = ef.getCodeLdap().replace(siNextCloudPrefix, "");
                if (siSuffix.contains("_")) {
                    siSuffix = siSuffix.split("_")[0];
                }
                if (StringUtils.isEmpty(siSuffix)) {
                    continue;
                }
                String kgLabel = siVwaPrefix + siSuffix;
                if (!graphDao.existGraphByName(kgLabel)) {
                    continue;
                }

                externalFolders.add(ef);
            }

        }

        return externalFolders;
    }

    private NextNumericDocumentDTO processActivityV2(ActivitiesDTO.ActivityDataDTO nextcloudActivity, UserEntity user, List<String> folderRacinesNames) throws IOException {
        NextNumericDocumentDTO dto = new NextNumericDocumentDTO();
        dto.setId((long) nextcloudActivity.getObjectId());
        dto.setNextCloudPath(nextcloudActivity.getObjectName());
        if (StringUtils.equals("systemtags", nextcloudActivity.getType())) {
            dto.setNextCloudPath(extractFilePathFromSubrich(nextcloudActivity.getSubjectRich()));
        }
        String nextCloudPath = dto.getNextCloudPath();
        if (StringUtils.isEmpty(nextCloudPath)) {
            return null;
        }
        //Vérification que le fichier est dans les dosser raicne de l'uttilisateur
        String[] splitPath = dto.getNextCloudPath().split("/");
        String folderRacine = "";
        if (splitPath.length >= 2) {
            folderRacine = splitPath[1];
        }
        if (StringUtils.isEmpty(folderRacine)) {
            return null;
        } else if (!folderRacinesNames.contains(folderRacine)) {
            return null;
        }

        //Rajout des autres propriétés du NextNumericDocumentDTO
        dto.setSi(folderRacine);
        System.out.println("NextCloudPath => " + dto.getNextCloudPath());
        dto.setLabel(nextCloudPath.substring(nextCloudPath.lastIndexOf("/") + 1));
        //http://nextcloud.dev.arkeup.com/index.php/apps/files/?dir=/DOSSIER_PACTE&openfile=3557
        dto.setLink(nextcloudUrl+"/index.php/apps/files/?dir="+nextCloudPath.substring(0, nextCloudPath.lastIndexOf("/"))
                +"&openfile="+nextcloudActivity.getObjectId());
        dto.setUrl(dto.getLink());

        //Alimentation de Activity
        Activity dtoActivity = new Activity();
        dtoActivity.setUser(nextcloudActivity.getUser());
        dtoActivity.setType(nextcloudActivity.getType());
        dtoActivity.setDatetime(nextcloudActivity.getDatetime());
        dto.setActivity(dtoActivity);
        //Récupération de tags et vérrification qu'il y a un tag "VWA"
        List<String> tags = getFileTagsSafe(String.valueOf(nextcloudActivity.getObjectId()));
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        } else if (!tags.contains("VWA")) {
            return null;
        } else {
            dto.setTags(tags);
        }
        //TODO : Récupération des customProperties
        //List<MultiStatusDTO.ResponseDTO> author = getAuthorsSafe(nextcloudActivity.getObjectName());
//        Map<String,String> customProperties = getCustomPropertiesFromNextCloud(nextcloudActivity.getObjectName(),nextcloudActivity.getUser());
//        dto.setLink(customProperties.get("link"));
//        dto.setDescription(customProperties.get("description"));
//        List<String> authors = new ArrayList<>();
//        authors.add(customProperties.get("author"));
//        authors.add(customProperties.get("auteur"));
//        authors.add(customProperties.get("autheur"));
//        dto.setAuthors(authors);

        return dto;
    }

    private String extractFilePathFromSubrich(List<Object> subrich) {
        String filePath = "";
        try {
            Object subrichObject = subrich.get(1);
            LinkedHashMap<String, Object> extractedSubjectDetails = (LinkedHashMap<String, Object>) subrichObject;
            LinkedHashMap<String, Object> extractedFileDetails = (LinkedHashMap<String, Object>) extractedSubjectDetails.get("file");
            filePath = (String) extractedFileDetails.get("path");
            filePath = "/" + filePath;
        } catch (Exception e) {
        }
        return filePath;
    }



    private List<String> getFileTagsSafe(String fileId) {
        try {
            return getFileTags(fileId);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la récupération des tags de fichier : " + e.getMessage());
            return null;
        }
    }

//    private Map<String, String>getAuthorsSafe(String objectName,String user) {
//        try {
//            return getCustomPropertiesFromNextCloud(objectName,user);
//        } catch (Exception e) {
//            LOGGER.severe("Erreur lors de la récupération des auteurs : " + e.getMessage());
//            return null;
//        }
//    }

    private NextNumericDocumentDTO createNextNumericDocumentDTO(FileDTO.Response fileDTO) {
        NextNumericDocumentDTO dto = new NextNumericDocumentDTO();
        if (fileDTO != null && fileDTO.getPropstat() != null) {
            for (FileDTO.Propstat propstat : fileDTO.getPropstat()) {
                FileDTO.Prop prop = propstat.getProp();
                if (prop != null) {
                    dto.setId(Long.valueOf(prop.getFileid()));
                    dto.setUrl(fileDTO.getHref());
                    return dto;
                }
            }
        } else {
            LOGGER.warning("fileDTO est null ou ne contient pas de réponses.");
        }
        return dto;
    }

    private List<ActivitiesDTO.ActivityDataDTO> getActivitiesForExternalFolder(HistorySync historySync) throws IOException {
        Long lastUserActivityID = historySync.getLastUserActivityID();
        List<ActivitiesDTO.ActivityDataDTO> activities = new ArrayList<>();
        Long maxActivityID = lastUserActivityID;
        boolean end = false;
        while (!end) {
            String requestUrl = nextcloudUrl + Activities + "?since=" + lastUserActivityID + "&format=json&sort=asc&limit=100";
            String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            String authHeader = "Basic " + encodedCredentials;

            Request request = new Request.Builder()
                    .url(requestUrl)
                    .get()
                    .addHeader("OCS-APIRequest", "true")
                    .addHeader("Authorization", authHeader)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    ActivitiesDTO ocsResponse = new ObjectMapper().readValue(responseBody, ActivitiesDTO.class);
                    activities.addAll(ocsResponse.getOcs().getData());

                    maxActivityID = lastUserActivityID;
                    try {
                        maxActivityID = Long.parseLong(response.header("X-Activity-Last-Given"));
                    } catch (Exception e) {
                        maxActivityID = lastUserActivityID;
                        end = true;
                    }
                    historySync.setLastUserActivityID(maxActivityID);
                    historySync.setLastSyncDate(new Date());
                    historySyncDao.createOrUpdate(historySync);
                    lastUserActivityID = maxActivityID;
                } else {
                    end = true;
                }
            } catch (IOException e) {
                LOGGER.severe("Erreur lors de la récupération des activités pour le dossier externe : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return activities;
    }

    private List<String> getFileTags(String fileId) throws IOException {
        String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String authHeader = "Basic " + encodedCredentials;
        MediaType mediaType = MediaType.parse("application/xml");
        String xmlBody = "<?xml version=\"1.0\"?>\n"
                + "<d:propfind  xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">\n"
                + "  <d:prop>\n"
                + "    <oc:display-name />\n"
                + "  </d:prop>\n"
                + "</d:propfind>";
        RequestBody body = RequestBody.create(mediaType, xmlBody);

        String requestUrl = nextcloudUrl + Webdav + Tags + fileId;
        Request request = new Request.Builder()
                .url(requestUrl.replace("//", "/"))
                .method("PROPFIND", body)
                .addHeader("OCS-APIRequest", "true")
                .addHeader("Content-Type", "application/xml")
                .addHeader("Authorization", authHeader)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();

        List<String> result = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Code de réponse inattendu : " + response);
            }
            result = parseXMLResponseTag(response.body().string());
        } catch (IOException e) {
            LOGGER.severe("Erreur lors de la récupération des tags de fichier : " + e.getMessage());
            throw e;
        }
        return result;
    }

    private List<String> parseXMLResponseTag(String xmlResponse) {
        List<String> result = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlResponse.getBytes("UTF-8"));
            Document doc = builder.parse(input);

            // Example of extracting custom properties
            NodeList propList = doc.getElementsByTagName("d:prop");
            for (int i = 0; i < propList.getLength(); i++) {
                Element prop = (Element) propList.item(i);
                String tag = prop.getTextContent();
                if (StringUtils.isNotEmpty(tag)) {
                    result.add(tag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<FileDTO.Response> getFileDetails(String user) throws IOException {
        String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String authHeader = "Basic " + encodedCredentials;
        MediaType mediaType = MediaType.parse("application/xml");
        RequestBody body = RequestBody.create(mediaType, "<?xml version=\"1.0\"?>\n" +
                "<d:propfind  xmlns:d=\"DAV:\"\n" +
                "    xmlns:oc=\"http://owncloud.org/ns\"\n" +
                "    xmlns:nc=\"http://nextcloud.org/ns\"\n" +
                "    xmlns:ocs=\"http://open-collaboration-services.org/ns\">\n" +
                "    <d:prop>\n" +
                "        <d:getlastmodified />\n" +
                "        <d:getetag />\n" +
                "        <d:getcontenttype />\n" +
                "        <d:resourcetype />\n" +
                "        <oc:fileid />\n" +
                "        <oc:permissions />\n" +
                "        <oc:size />\n" +
                "        <d:getcontentlength />\n" +
                "        <nc:has-preview />\n" +
                "        <nc:mount-type />\n" +
                "        <nc:is-encrypted />\n" +
                "        <ocs:share-permissions />\n" +
                "        <oc:tags />\n" +
                "        <oc:favorite />\n" +
                "        <oc:comments-unread />\n" +
                "        <oc:owner-id />\n" +
                "        <oc:owner-display-name />\n" +
                "        <oc:share-types />\n" +
                "    </d:prop>\n" +
                "</d:propfind>");

        Request request = new Request.Builder()
                .url(nextcloudUrl + Webdav + "files/" + username)
                .method("PROPFIND", body)
                .addHeader("OCS-APIRequest", "true")
                .addHeader("Content-Type", "application/xml")
                .addHeader("Authorization", authHeader)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Code de réponse inattendu : " + response);
            }
            String xmlResponse = response.body().string();
            XmlMapper xmlMapper = new XmlMapper();
            FileDTO fileDTO = xmlMapper.readValue(xmlResponse, FileDTO.class);

            return fileDTO.getResponses();
        } catch (IOException e) {
            LOGGER.severe("Erreur lors de la récupération des détails de fichier : " + e.getMessage());
            throw e;
        }
    }


    private Map<String, String> getCustomPropertiesFromNextCloud(String objectName,String user) throws IOException {
        String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String authHeader = "Basic " + encodedCredentials;
        MediaType mediaType = MediaType.parse("application/xml");

        // Construction de la requête XML
        String xmlRequestBody = "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\" xmlns:nc=\"http://nextcloud.org/ns\">\n" +
                "    <d:prop>\n" +
                "        <d:getetag/>\n" +
                "        <d:getcontenttype/>\n" +
                "        <oc:author/>\n" +
                "        <oc:auteur/>\n" +
                "        <oc:autheur/>\n" +
                "        <oc:description/>\n" +
                "        <oc:link/>\n" +
                "    </d:prop>\n" +
                "</d:propfind>";

        RequestBody body = RequestBody.create(mediaType, xmlRequestBody);

        String requestUrl = nextcloudUrl + "/remote.php/dav/files/" + user + objectName;
        Request request = new Request.Builder()
                .url(requestUrl)
                .method("PROPFIND", body)
                .addHeader("accept","application/json, text/plain, */*")
                .addHeader("OCS-APIRequest", "true")
                .addHeader("Content-Type", "application/xml;charset=UTF-8")
                .addHeader("Authorization", authHeader)
                .addHeader("accept-encoding","gzip, deflate")
                .build();

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
        Map<String, String> result = new HashMap<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Code de réponse inattendu : " + response);
            }
            result = parseXMLResponseAuth(response.body().string());
        } catch (IOException e) {
            LOGGER.severe("Erreur lors de la récupération des propriétés personnalisées : " + e.getMessage());
            throw e;
        }
        return result;
    }

    private Map<String, String> parseXMLResponseAuth(String xmlResponse) {
        Map<String, String> authResult = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(input);

            // Normalisation du document pour assurer une structure uniforme
            doc.getDocumentElement().normalize();

            // Extraction des propriétés personnalisées
            NodeList propList = doc.getElementsByTagName("d:prop");
            for (int i = 0; i < propList.getLength(); i++) {
                Element prop = (Element) propList.item(i);
                NodeList children = prop.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node node = children.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String nodeName = node.getNodeName().trim(); // Retrait des espaces superflus
                        String nodeValue = node.getTextContent().trim();

                        // Vérification de la non-nullité et de la non-vacuité de la valeur avant de l'ajouter au résultat
                        if (StringUtils.isNotEmpty(nodeValue)) {
                            authResult.put(nodeName, nodeValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du parsing du XML : " + e.getMessage());
            e.printStackTrace();
        }
        return authResult;
    }


    @Override
    public Map<Long, String> getUserFromLiferay() {
        ObjectMapper mapper = new ObjectMapper();
        String url = liferayPortalUrl + "/api/jsonws/foo.numdocuser/get-user-list";
        String output = "";
        Map<Long, String> userOutputMap = new HashMap<>();
        List<UserFromLiferayDto> userFromLiferayDtoList = new ArrayList();

        try {
            output = GetWs.doGet(url, liferayPortalUserLogin, liferayPortalUserPassword);
            System.out.println("output:" + output);
            userFromLiferayDtoList = Arrays.asList(mapper.readValue(output, UserFromLiferayDto[].class
            ));
            saveUsersFromLiferay(userFromLiferayDtoList);
            for (UserFromLiferayDto userFromLiferayDto : userFromLiferayDtoList) {
                userOutputMap.put(Long.parseLong(userFromLiferayDto.getId()), userFromLiferayDto.getLogin());
            }

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ResourceSAImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return userOutputMap;
    }

    @Override
    public List<ResourceScanDTO> getResourceList(String label, String creator, String type, String debutString, String finString, String kgLabel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserEntity creatorEntity = null;
        ResourceTypeEntity typeEntity = null;
        Date dateDeb = null;
        Date dateFin = null;
        UserEntity user = resourceDao.getUserEntityByLabel(username);
        if (Objects.isNull(user)) {
            return new ArrayList<>();
        }
        if (StringUtils.isNotEmpty(creator)) {
            creatorEntity = resourceDao.getUserEntityByLabel(creator);
        }
        if (StringUtils.isNotEmpty(type)) {
            typeEntity = resourceTypeDao.getTypeByName(type);
        }
        if (StringUtils.isNotEmpty(debutString)) {
            try {
                dateDeb = DateUtils.parseDate(debutString, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
            } catch (Exception e) {
                dateDeb = null;
            }
        }
        if (StringUtils.isNotEmpty(finString)) {
            try {
                dateFin = DateUtils.parseDate(finString, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
            } catch (Exception e) {
                dateFin = null;
            }
        }

        List<ResourceEntity> entities = resourceDao.getResourceList(label, creatorEntity, typeEntity, dateDeb, dateFin);
        List<ExternalFolder> foldersByUser = userFolderDAO.getFoldersByUser(user);
        return entities
                .stream()
                .filter(resource -> isResourceVisible(foldersByUser, resource))
                .map(resource -> resourceConverter.convertEntityToDTO(resource))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param externalFolders
     */
    private void showFolder(List<ExternalFolder> externalFolders) {
        for (ExternalFolder externalFolder : externalFolders) {
            logger.info(externalFolder.getId() + " ===> " + externalFolder.getLabel());
        }
    }

    private Boolean isResourceVisible(List<ExternalFolder> foldersByUser, ResourceEntity entity) {
        if (entity instanceof NumericDocumentEntity) {
            NumericDocumentEntity document = (NumericDocumentEntity) entity;
            if (Objects.nonNull(document.getFolder())) {
                logger.info(String.valueOf(foldersByUser.contains(document.getFolder())));
                if (!foldersByUser.contains(document.getFolder())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveUsersFromLiferay(List<UserFromLiferayDto> userDtos) {
        for (UserFromLiferayDto userDto : userDtos) {
            ResourceEntity unicityTest = resourceDao.getResourceByLabel(userDto.getLogin());
            if (unicityTest != null) {
                continue;
            }

            Optional<UserEntity> userEntityOptional = userDao.findByLogin(userDto.getLogin());
            if (!userEntityOptional.isPresent()) {
                UserEntity userEntity = new UserEntity();
                userEntity.setLogin(userDto.getLogin());
                userEntity.setLabel(userDto.getLogin());
                userEntity.setMail(userDto.getMail());
                userEntity.setPassword("");
                resourceDao.saveResource(userEntity);
            }
        }
    }

    private String formatDateForUrl(String sDate) {
        String output = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(sDate);
            output = date.getTime() / 1000 + "";
        } catch (Exception e) {
            System.out.println(e);
        }
        return output;
    }

    @Override
    public Map<Long, String> getEmitter(String kgLabel) {
        Map<Long, String> agentList = new HashMap<>();
        GraphEntity graphEntity = graphDao.getGraphByName(kgLabel);
        if (graphEntity == null) {
            return new HashMap<>();
        }
        List<ResourceEntity> agentListEntity = viewpointDao.getGraphEmitter(graphEntity);
        if (!Objects.isNull(agentListEntity)) {
            for (ResourceEntity agent : agentListEntity) {
                agentList.put(agent.getId(), agent.getLabel());
            }
        }
        return agentList;
    }

    @Override
    public List<ResourceEntity> retrieveUserFromGraphAndUserGroup(GraphEntity graph, ResourceEntity ugroup) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserEntity user = resourceDao.getUserEntityByLabel(username);
        if (Objects.isNull(user)) {
            return new ArrayList<>();
        }

        List<ViewpointEntity> viewpointEntitys = viewpointDao.getViewpointsByGraphAndUGroup(ugroup, graph);
        List<ResourceEntity> result = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(viewpointEntitys)) {
            for (ViewpointEntity viewpoint : viewpointEntitys) {
                if (!viewpoint.getResource1().equals(ugroup) && !result.contains(viewpoint.getResource1())) {
                    result.add(viewpoint.getResource1());
                }
                if (!viewpoint.getResource2().equals(ugroup) && !result.contains(viewpoint.getResource2())) {
                    result.add(viewpoint.getResource2());
                }
            }
        }

        return result;
    }

    @Override
    public String confirmResourceRight(List<Long> ids) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        ResourceEntity user = resourceDao.getResourceByLabel(username);
        if (Objects.isNull(user)) {
            return messageSource.resolveCode("RESOURCE_DELETE_ASKING_ERROR", LabelConstant.RESOURCE_DELETE_ASKING_ERROR);
        }

        String result = "";
        for (Long id : ids) {
            ResourceEntity resource = resourceDao.getResourceById(id);
            if (Objects.isNull(resource)) {
                result += messageSource.resolveCode("RESOURCE_DELETE_ASKING_ERROR", LabelConstant.RESOURCE_DELETE_ASKING_ERROR) + " with id " + resource.getLabel() + "\n";;
            }

            if (Objects.nonNull(resource.getCreator()) && !resource.getCreator().equals(user)) {
                result += messageSource.resolveCode("RESOURCE_NOT_CREATOR", LabelConstant.RESOURCE_NOT_CREATOR) + " " + resource.getLabel() + "\n";
            } else {
                result += messageSource.resolveCode("RESOURCE_CREATOR", LabelConstant.RESOURCE_CREATOR) + " " + resource.getLabel() + "\n";
            }
        }
        return result;
    }

    @Override
    public Map<String, String> namesWithTypes() {
        List<ResourceEntity> resourcesList = resourceDao.getAllResources();
        return resourcesList.stream()
                .filter(resource -> resource.getLabel() != null)
                .collect(Collectors.toMap(
                        ResourceEntity::getLabel,
                        resource -> resource.getType() != null && resource.getType().getName() != null
                                ? resource.getType().getName()
                                : "UnknownType"
                ));
    }


    @Override
    public String addDynamicLoosenVwa(List<String> resource1, String resource2, String kgLabel) {
        String output = "";
        ResourceEntity res2 = resourceDao.getResourceByLabel(resource2);

        if (Objects.isNull(res2)) {
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emitterString = authentication.getName();
        ResourceEntity emitter = resourceDao.getResourceByLabel(emitterString);

        if (Objects.isNull(emitter)) {
            return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
        }

        ViewpointWebApp vw = new ViewpointWebApp();
        for (String resource : resource1) {

            ResourceEntity res1 = resourceDao.getResourceByLabel(resource);
            if (Objects.isNull(res1)) {
                continue;
            }
            vw.setEmitter(emitterString);
            vw.setResource1(resource);
            vw.setResource2(resource2);
            vw.setType(ViewpointTypeConstant.LOOSEN);
            vw.setTargetKG(kgLabel);
            output = viewpointSA.addViewPoint(vw);
            if (output.equals(messageSource.resolveCode("VIEWPOINT_FAILED", LabelConstant.VIEWPOINT_FAILED))) {
                return messageSource.resolveCode("RESOURCE_CREATION_ERROR", LabelConstant.RESOURCE_CREATION_ERROR);
            }
        }

        return output;
    }
}
