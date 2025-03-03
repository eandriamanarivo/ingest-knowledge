package com.viewpoint.webapplication.services.applicatif.resource;

import java.util.*;

import com.viewpoint.webapplication.data.domainobject.NumericDocumentWebApp;
import com.viewpoint.webapplication.data.domainobject.ResourceCSV;
import com.viewpoint.webapplication.data.domainobject.ResourceWebApp;
import com.viewpoint.webapplication.data.dto.PreviewDto;
import com.viewpoint.webapplication.data.dto.ResourceScanDTO;
import com.viewpoint.webapplication.data.dto.ResponseDTO;
import com.viewpoint.webapplication.data.entityclass.graph.GraphEntity;
import com.viewpoint.webapplication.data.entityclass.resource.ResourceEntity;
import com.viewpoint.webapplication.data.entityclass.resource.UserEntity;

/**
 * Interface ResourceSA
 *
 * @author a.safidy
 * @version 1.0
 */
public interface ResourceSA {

    /**
     * retourne une liste de nom d'agent contenu dans GraphHolder selon le
     * kgLabel
     *
     * @param kgLabel: label d'un agent
     * @return name : nom retourné
     */
    List<String> agentNames(String kgLabel);

    /**
     * retourne une liste de nom compris dans un graphe selon son kgLabel
     *
     * @param kgLabel : label du graphe de connaissance
     * @return ArrayList de nom
     */
    List<String> names(String kgLabel);

    List<String> names(String kgLabel, List<String> types);

    /**
     * retourne le nom d'un graphe de connaissance
     *
     * @return name
     */
    List<String> kgs(boolean demo);

    List<GraphEntity> findAllKg(boolean demo);

    List<GraphEntity> findAllKgForUserConnected(UserEntity userEntity, boolean demo);

    /**
     * @param label : label d'une resoource recherché
     * @param username : user de la resoource
     * @param kgLabel : label du graphe
     * @return localKM.getLocalKM(search, 12)
     */
    String localKM(String label, String username, String kgLabel);

    /**
     *
     * @param label
     * @param username
     * @param method
     * @param m
     * @param p
     * @param deb
     * @param fin
     * @param kgLabel
     * @return 
     */
    LinkedList<String> search(String label, String username, String method, float m, float p, String deb, String fin, String kgLabel);

    public LinkedList<String> search(String label, String username, 
            String method, float m, float p, String deb, String fin, String kgLabel, int fitlerUserGroup, Long resUserGroup);

    /**
     * test d'existence et ajout de ressource
     *
     * @param resource :ressource a ajouter
     * @return LabelConstant : de statut d'existence
     */
    String addResource(ResourceWebApp resource);

    public String addResourceCsv(ResourceCSV resource);

    /**
     * SESSION DEMO test d'existence et ajout de ressource
     *
     * @param resource :ressource a ajouter
     * @return LabelConstant : de statut d'existence
     */
    String addResourceDemo(ResourceWebApp resource);

    /**
     * sauvegarde de l'Url d'une ressource dans un GraphHolder
     *
     * @param label label de la ressource
     * @param url a sauvegarder
     * @param kgLabel label d'un graphe
     * @return LabelConstant
     */
    String saveUrl(String label, String url, String description);

    /**
     * retourne un Url
     *
     * @param NumDoc : Url
     * @return resource : ressource d'une Url
     */
    String setURL(NumericDocumentWebApp NumDoc);

    /**
     * @param NumDoc
     * @return
     */
    String getURL(String label, String targetKG);

    /**
     * retourne une liste de classe des resources existant
     *
     * @return classList : liste des classes
     * @throws ClassNotFoundException : erreur montrant une un manque de
     * dependance
     */
    ArrayList<String> getResourceList() throws ClassNotFoundException;

    /**
     * retourne l'Url de la ressource precedante resource
     *
     * @param label : label de la ressource
     * @param kgLabel : label du graphe étudié
     * @return LabelConstant
     */
    String getResourcePreview(String label, List<String> kgNames, String kgLabel);

    /**
     * retourne le type classe de resource
     *
     * @param name nome de la ressource
     * @param kgLabel : label du graphe étudié
     * @return ResourceType
     */
    String getResourceType(String name, String kgLabel);

    /**
     * recherche du plus court chemin
     *
     * @param label1 : label d'une ressource1
     * @param label2 : labe d'une ressource2
     * @param username : identifie le resurce a observer
     * @param kgLabel : label du graphe étudié
     * @return jsonpath chemin trouvé
     */
    String shortestPath(String label1, String label2, String username, String kgLabel);

    Map<String, String> getResourceTypeList();

    Map<String, String> getResourceTypeColor();

    ResourceEntity getResourceByName(String name, String kgLabel);

    Map<String, Float> computeAndshort(String label, String username, String method, float m, float p, String deb, String fin, String kgLabel, int fitlerUserGroup, Long resUserGroup);

    Map<String, Float> computeAndshort(String label, String username, String method, float m, float p, String deb, String fin, String kgLabel);

    Boolean isUser(ResourceEntity r);

    public PreviewDto getPreview(String resource, List<String> kgNames, String kgLabel);

    public Map<Long, String> getAllResources(String kgLabel);

    public Map<Long, String> getResourcesByIsUser(String kgLabel, Boolean isUser);

    public Map<Long, String> getAllUsers(String kgLabel);

    public Map<Long, String> getResourcesByGroup(Long igGroup);

    public List<ResourceEntity> getResourcesKernelByGroup(Long group, String kgLabel);

    public List<ResourceEntity> getResourcesKernelByList(List<String> labelList, String kgLabel);

    public Map<Long, String> getFolderFromLiferay();

    public Map<Long, String> getFolderFromLiferay(String username);

    public String getResourceFromLiferay(Long folderid, Long userId, String sDateFrom, String sDateTo, String kgLabel);

    public String getResourceFromLiferay(Long folderid, String username, String sDateFrom, String sDateTo, String kgLabel);

    public Map<Long, String> getUserFromLiferay();

    public String saveEvent(String label, Float valueX, Float valueY, String sDateDebut, String sDateFin);

    public List<ResourceScanDTO> getResourceList(String label, String creator, String type, String debutString, String finString, String kgLabel);

    public String deleteResource(List<Long> id);

    Set<String> getHistoryVisuInitByKgLabel(String kgLabel);

    ResponseDTO getDefaultRadius(String kgLabel);

    public Map<Long, String> getEmitter(String kgLabel);

    public LinkedList<String> shortestPathMultisearch(String label1, String label2, String username, float m, float p, String deb, String fin, String kgLabel, Long agroup, int filter, int fitlerUserGroup, Long userGroup);

    public String saveUserGroup(String label, List<String> agent, String userGroupType, String targetKg, String emitter, String action);

    public Map<Long, String> getAllUserGroups(String kgLabel);

    public List<ResourceEntity> retrieveUserFromGraphAndUserGroup(GraphEntity graph, ResourceEntity resUserGroupEntity);

    public String getResourceFromNextCloud(String kgLabel);

    public Map<Long, String> getAllTopics(String kgLabel);

    public String confirmResourceRight(List<Long> ids);

    public Map<String, String> namesWithTypes();
    
    public String addDynamicLoosenVwa(List<String> resource1, String resource2, String kgLabel);
    
    public void updateSrenths(String kgLabel, HashMap<String, Float> typesStrenghts);

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
    );

}
