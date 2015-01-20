/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/HYPERIMAGE.LICENSE
 * or http://www.sun.com/cddl/cddl.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/HYPERIMAGE.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2009 Humboldt-Universitaet zu Berlin
 * All rights reserved.  Use is subject to license terms.
 */

/*
 * Copyright 2014 Leuphana Universität Lüneburg
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.client.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataName;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiKeyValue;
import org.hyperimage.client.ws.HiLanguage;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiProjectMetadata;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.hyperimage.client.xmlimportexport.XMLImporter;

/**
 * @author Jens-Martin Loebel
 */
public class MetadataHelper {
    private static final Charset syscharset = Charset.forName(System.getProperty("file.encoding"));
    private static final Charset utf8charset = Charset.forName("UTF-8");
    private static final CharsetDecoder decoder = syscharset.newDecoder();
    private static final CharsetEncoder encoder = utf8charset.newEncoder();

    public static List<HiFlexMetadataRecord> resolveMetadataRecords(HiBase base) {
        List<HiFlexMetadataRecord> records = null;

        if (!(base instanceof Hiurl)) {
            records = base.getMetadata();
        } else {
            records = new ArrayList<HiFlexMetadataRecord>();
        }

        return records;
    }

    public static HiFlexMetadataRecord getDefaultMetadataRecord(HiBase base, HiLanguage language) {
        return getDefaultMetadataRecord(base, language.getLanguageId());
    }

    public static HiFlexMetadataRecord getDefaultMetadataRecord(HiBase base, String language) {
        HiFlexMetadataRecord record = null;

        if (base == null) {
            return record;
        }
        if (language == null) {
            return record;
        }

        List<HiFlexMetadataRecord> records = resolveMetadataRecords(base);

        for (HiFlexMetadataRecord rec : records) {
            if (rec.getLanguage().compareTo(language) == 0) {
                record = rec;
            }
        }

        return record;
    }

    public static HiFlexMetadataRecord getDefaultMetadataRecord(List<HiFlexMetadataRecord> records, String language) {
        HiFlexMetadataRecord record = null;

        if (records == null) {
            return record;
        }

        for (HiFlexMetadataRecord rec : records) {
            if (rec.getLanguage().compareTo(language) == 0) {
                record = rec;
            }
        }

        return record;
    }

    public static String findValue(HiFlexMetadataTemplate template, String key, HiFlexMetadataRecord record) {
        return findValue(template.getNamespacePrefix(), key, record);
    }

    public static String findValue(String template, String key, HiFlexMetadataRecord record) {
        String value = null;

        if (record == null) {
            return null;
        }

        for (HiKeyValue kvPair : record.getContents()) {
            if (kvPair.getKey().compareTo(template + "." + key) == 0) {
                value = kvPair.getValue();
            }
        }

        return value;
    }

    public static boolean setValue(HiFlexMetadataTemplate template, String key, String value, HiFlexMetadataRecord record) {
        return setValue(template.getNamespacePrefix(), key, value, record);
    }

    public static boolean setValue(String template, String key, String value, HiFlexMetadataRecord record) {
        for (HiKeyValue kvPair : record.getContents()) {
            if (kvPair.getKey().compareTo(template + "." + key) == 0) {
                kvPair.setValue(value);
                return true;
            }
        }

        return false;
    }

    public static String richTextToHTML(String richText) {
        HIRichText text = new HIRichText(richText);
        return text.getHTMLModel();
    }
    
    public static HiFlexMetadataTemplate getTemplateByNSPrefix(String nsPrefix) {
        for (HiFlexMetadataTemplate projTemplate : HIRuntime.getManager().getProject().getTemplates())
            if ( projTemplate.getNamespacePrefix().compareTo(nsPrefix) == 0 ) return projTemplate;
        
        return null;
    }

    public static String getTemplateKeyDisplayName(HiFlexMetadataTemplate template, HiFlexMetadataSet keySet, String guiLang) {
        String displayName = null;

        // if no display name can be found use the tag name
        displayName = keySet.getTagname();

        // check known sets if possible
        if (template != null && template.getNamespacePrefix().compareTo("HIInternal") == 0) {
            // TODO - clean up for easier internationalization
            if (guiLang.compareTo("de") == 0) {
                if (keySet.getTagname().compareTo("catchall") == 0) {
                    displayName = "Zusatz";
                }
                if (keySet.getTagname().compareTo("note") == 0) {
                    displayName = "Notiz";
                }
            }
        } else {
            // search for correct display name
            for (HiFlexMetadataName name : keySet.getDisplayNames()) {
                if (name.getLanguage().compareTo(guiLang) == 0) {
                    displayName = name.getDisplayName();
                }
            }
        }

        return displayName;
    }

    public static boolean setHasDisplayNameEntry(HiFlexMetadataSet set, String guiLang) {
        for (HiFlexMetadataName name : set.getDisplayNames()) {
            if (name.getLanguage().compareTo(guiLang) == 0) {
                return true;
            }
        }

        return false;
    }

    public static String getTemplateKeyDisplayName(HiFlexMetadataTemplate template, String key, String guiLang) {
        HiFlexMetadataSet keySet = null;

        // find key
        for (HiFlexMetadataSet entry : template.getEntries()) {
            if (entry.getTagname().compareTo(key) == 0) {
                keySet = entry;
            }
        }
        // key not found in template --> return
        if (keySet == null) {
            return key;
        }

        return getTemplateKeyDisplayName(template, keySet, guiLang);
    }
    
    public static HiFlexMetadataSet findTemplateEntryByTagName(String tagName, HiFlexMetadataTemplate template) {
        if ( tagName == null || template == null ) return null;
        
        for ( HiFlexMetadataSet entry : template.getEntries() )
            if ( entry.getTagname().compareTo(tagName) == 0 ) return entry;

        return null;
    }
    

    public static HiPreference findPreference(HiProject project, String key) {
        for (HiPreference pref : project.getPreferences()) {
            if (pref.getKey().compareTo(key) == 0) {
                return pref;
            }
        }

        return null;
    }

    public static String findPreferenceValue(HiProject project, String key) {
        for (HiPreference pref : project.getPreferences()) {
            if (pref.getKey().compareTo(key) == 0) {
                return pref.getValue();
            }
        }

        return null;
    }

    public static boolean setPreferenceValue(HiProject project, String key, String value) {
        for (HiPreference pref : project.getPreferences()) {
            if (pref.getKey().compareTo(key) == 0) {
                pref.setValue(value);
                return true;
            }
        }

        return false;
    }

    public static String getFullyQualifiedURI(HiQuickInfo info) {
        if ( info.getUUID() != null ) return info.getUUID();
        return getProjectFullyQualifiedURI() + "/" + getDisplayableID(info);
    }

    public static String getFullyQualifiedURI(HiBase base) {
        if ( base.getUUID() != null ) return base.getUUID();
        return getProjectFullyQualifiedURI() + "/" + getDisplayableID(base);
    }
    
    public static String getBaseIDQualifiedURI(HiQuickInfo info) {
        return getProjectFullyQualifiedURI() + "/" + getDisplayableID(info, true);
    }

    public static String getBaseIDQualifiedURI(HiBase base) {
        return getProjectFullyQualifiedURI() + "/" + getDisplayableID(base, true);
    }
    
    public static String getProjectFullyQualifiedURI() {
        String uriString;
        String serverURL = HIRuntime.getManager().getServerURL();
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        uriString = serverURL + "project/P" + HIRuntime.getManager().getProject().getId();

        return uriString;
    }

    public static String getDisplayableID(HiQuickInfo info) {
        return getDisplayableID(info, false);
    }
    public static String getDisplayableID(HiQuickInfo info, boolean disableUUID) {
        String dispID = "";
        if (info != null) {
            if (info.getContentType() == HiBaseTypes.HI_GROUP) {
                dispID = "G";
            }
            if (info.getContentType() == HiBaseTypes.HI_INSCRIPTION) {
                dispID = "I";
            }
            if (info.getContentType() == HiBaseTypes.HI_LAYER) {
                dispID = "L";
            }
            if (info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE) {
                dispID = "X";
            }
            if (info.getContentType() == HiBaseTypes.HI_OBJECT) {
                dispID = "O";
            }
            if (info.getContentType() == HiBaseTypes.HI_TEXT) {
                dispID = "T";
            }
            if (info.getContentType() == HiBaseTypes.HI_VIEW) {
                dispID = "V";
            }
            if (info.getContentType() == HiBaseTypes.HIURL) {
                dispID = "U";
            }
            if ( info.getUUID() == null || disableUUID == true ) dispID = dispID + info.getBaseID();
            else dispID = info.getUUID();
        }
        return dispID;
    }

    public static String getDefaultLightTableXML() {
        final String xml = "<lita id =\"\"><title xml:lang=\"\" /></lita>";
        return xml;
    }

    public static String convertToUTF8(String input) {
        String output = input;
        try {
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(input));
            CharBuffer cbuf = decoder.decode(bbuf);
            output = cbuf.toString();
        } catch (CharacterCodingException e) {
            // ignore, return input
        }

        return output;
    }

    public static String getDisplayableID(HiBase base) {
        return getDisplayableID(base, false);
    }
    
    public static String getDisplayableID(HiBase base, boolean disableUUID) {
        String dispID = "";

        if (base != null) {
            if (base instanceof HiGroup) {
                dispID = "G";
            }
            if (base instanceof HiInscription) {
                dispID = "I";
            }
            if (base instanceof HiLayer) {
                dispID = "L";
            }
            if (base instanceof HiLightTable) {
                dispID = "X";
            }
            if (base instanceof HiObject) {
                dispID = "O";
            }
            if (base instanceof HiText) {
                dispID = "T";
            }
            if (base instanceof Hiurl) {
                dispID = "U";
            }
            if (base instanceof HiView) {
                dispID = "V";
            }

            if ( base.getUUID() == null || disableUUID == true ) dispID = dispID + base.getId();
            else dispID = base.getUUID();
        }

        return dispID;
    }

    public static String findValue(HiProject project, String languageId) {
        String title = null;
        if (project == null || project.getMetadata() == null) {
            return title;
        }

        for (HiProjectMetadata metadata : project.getMetadata()) {
            if (metadata.getLanguageID().compareTo(languageId) == 0) {
                title = metadata.getTitle();
            }
        }

        return title;
    }

    public static boolean setValue(HiProject project, String languageId, String title) {
        if (project == null || project.getMetadata() == null) {
            return false;
        }

        for (HiProjectMetadata metadata : project.getMetadata()) {
            if (metadata.getLanguageID().compareTo(languageId) == 0) {
                metadata.setTitle(title);
                return true;
            }
        }

        return false;
    }

    public static Locale langToLocale(HiLanguage hiLang) {
        return langToLocale(hiLang.getLanguageId());
    }

    public static Locale langToLocale(String language) {
        if (language.indexOf("_") >= 0) {
            return new Locale(
                    language.substring(0, language.indexOf('_')),
                    language.substring(language.indexOf('_') + 1));
        }

        return new Locale(language);
    }

    public static String localeToLangID(Locale lang) {
        String langID = lang.getLanguage();
        if (lang.getCountry().length() > 0 && !lang.getCountry().equalsIgnoreCase(langID)) {
            langID = langID + "_" + lang.getCountry();
        }
        langID = langID.toLowerCase();

        return langID;
    }

    
    // ----------------------------------------------------------------------
    
    private static void addMetadataSet(
            HiFlexMetadataTemplate metadataTemplate,
            boolean isRichText, 
            String strTagName,
            String[][] arrLanguageName) {
        HiFlexMetadataSet metadataSet = new HiFlexMetadataSet();
        
        metadataSet.setRichText(isRichText);
        metadataSet.setTagname(strTagName);
        
        for (String[] arrLanguageNameEntry : arrLanguageName) {
            HiFlexMetadataName metadataName = new HiFlexMetadataName();
            metadataName.setLanguage(arrLanguageNameEntry[0]);
            metadataName.setDisplayName(arrLanguageNameEntry[1]);
            metadataSet.getDisplayNames().add(metadataName);
        }
   
        metadataTemplate.getEntries().add(metadataSet);
    }
    
    /**
     * Insert VRA Core 4 metadata. Based on VRA Core 4.0 Element Description dated
     * 04/05/2007 (I assume that's the USA date format).
     * See <http://www.loc.gov/standards/vracore/VRA_Core4_Element_Description.pdf>
     * for details (last retrieved 2014-05-30).
     * 
     * @return VRA Core 4 metadata template.
     */
    public static HiFlexMetadataTemplate getVRACore4TemplateBlueprint() {
        HiFlexMetadataTemplate vra4Template = new HiFlexMetadataTemplate();
        vra4Template.setNamespacePrefix("vra4"); //$NON-NLS-1$
        vra4Template.setNamespaceURI(XMLImporter.VRA_4_XMLNS); //$NON-NLS-1$
        vra4Template.setNamespaceURL(XMLImporter.VRA_4_XMLNS); //$NON-NLS-1$
        String[][] strarrLangName = null;
        
        // Only implementing WORK as metadata on a HyperImage object for the time being.
        strarrLangName = new String[][]{
            {"en","Work"},
            {"de","Werk"}
        };
        addMetadataSet(vra4Template, true, "work", strarrLangName);
           
/*        
        ////////////////// WORK //////////////////
        // no data values for element <work/>, but contains subelements.
        // –> id
        strarrLangName = new String[][]{
            {"en","work.id"},
            {"de","Werk.id"}
        };
        addMetadataSet(vra4Template, false, "work.id", strarrLangName);
        // –> refid
        strarrLangName = new String[][]{
            {"en","work.refid"},
            {"de","Werk.refid"}
        };
        addMetadataSet(vra4Template, false, "work.refid", strarrLangName);
        // –> source
        strarrLangName = new String[][]{
            {"en","work.source"},
            {"de","Werk.Quelle"}
        };
        addMetadataSet(vra4Template, false, "work.source", strarrLangName);
*/        
        ////////////////// COLLECTION //////////////////
        // no data values for element <collection/>, but contains subelements.
        // –> id
        // –> refid
        // –> source
        
        ////////////////// IMAGE //////////////////
        // no data values for element <image/>, but contains subelements.
        // –> id
        // –> refid
        // –> source
       
        ////////////////// AGENT //////////////////
        // –> agent
        // –> agent –> name
        // –> agent –> name –> type
        // –> agent –> name –> vocab
        // –> agent –> name –> refid
        // –> agent –> culture
        // –> agent –> dates
        // –> agent –> dates –> type
        // –> agent –> dates –> type –> earliestDate
        // –> agent –> dates –> type –> latestDate
        // –> agent –> role
        // –> agent –> role –> vocab
        // –> agent –> role –> refid
        // –> agent –> attribution
        strarrLangName = new String[][]{
            {"en","Agent"},
            {"de","Agent"}
        };
        addMetadataSet(vra4Template, true, "agent", strarrLangName);
        
        ////////////////// CULTURAL CONTEXT //////////////////
        // –> culturalContext
        // –> culturalContext –> vocab
        // –> culturalContext –> refid
        strarrLangName = new String[][]{
            {"en","Cultural Context"},
            {"de","Kultureller Kontext"}
        };
        addMetadataSet(vra4Template, true, "culturalContext", strarrLangName);
        
        ////////////////// DATE //////////////////
        // –> display
        // –> date –> type
        // –> date –> type –> earliestDate
        // –> date –> type –> latestDate
        // –> date –> source
        // –> date –> href
        // –> date –> dataDate
        strarrLangName = new String[][]{
            {"en","Date"},
            {"de","Datum"}
        };
        addMetadataSet(vra4Template, true, "date", strarrLangName);
        
        ////////////////// DESCRIPTION //////////////////
        // –> display
        // –> description
        // –> description –> source
        strarrLangName = new String[][]{
            {"en","Description"},
            {"de","Beschreibung"}
        };
        addMetadataSet(vra4Template, true, "description", strarrLangName);
        
        
        ////////////////// INSCRIPTION //////////////////
        // –> display
        // –> inscription –> author
        // –> inscription –> author –> vocab
        // –> inscription –> author –> refid
        // –> inscription –> position
        // –> inscription –> text
        // –> inscription –> text –> type
        // –> inscription –> text –> xml:lang
        
        ////////////////// LOCATION //////////////////
        // -> display
        // -> location –> type
        // -> location –> type –> name
        // -> location –> type –> name –> type
        // -> location –> type –> name –> xml:lang
        // -> location –> type –> refid
        // -> location –> type –> refid –> type
        // -> location –> type –> name
        // -> location –> type –> name –> type
        // -> location –> type –> name –> vocab
        // -> location –> type –> name –> refid
        // -> location –> type –> name –> extent
        strarrLangName = new String[][]{
            {"en","Location"},
            {"de","Ort"}
        };
        addMetadataSet(vra4Template, true, "location", strarrLangName);
        
        
        ////////////////// MATERIAL //////////////////
        // –> display
        // –> material
        // –> material –> type
        // –> material –> vocab
        // –> material –> refid
        strarrLangName = new String[][]{
            {"en","Material"},
            {"de","Material"}
        };
        addMetadataSet(vra4Template, true, "material", strarrLangName);
        
        ////////////////// MEASUREMENTS //////////////////
        strarrLangName = new String[][]{
            {"en","Measurements"},
            {"de","Maße"}
        };
        addMetadataSet(vra4Template, true, "measurements", strarrLangName);
        
        ////////////////// RELATION //////////////////
        strarrLangName = new String[][]{
            {"en","Relation"},
            {"de","Beziehung"}
        };
        addMetadataSet(vra4Template, true, "relation", strarrLangName);
        
        ////////////////// RIGHTS //////////////////
        strarrLangName = new String[][]{
            {"en","Rights"},
            {"de","Rechte"}
        };
        addMetadataSet(vra4Template, true, "rights", strarrLangName);
        
        ////////////////// SOURCE //////////////////
        strarrLangName = new String[][]{
            {"en","Source"},
            {"de","Quelle"}
        };
        addMetadataSet(vra4Template, true, "source", strarrLangName);
        
        ////////////////// STATE EDITION //////////////////
        strarrLangName = new String[][]{
            {"en","State / Edition"},
            {"de","Zustand / Edition"}
        };
        addMetadataSet(vra4Template, true, "stateEdition", strarrLangName);
        
        ////////////////// STYLE PERIOD //////////////////
        strarrLangName = new String[][]{
            {"en","Style / Period"},
            {"de","Stil / Periode"}
        };
        addMetadataSet(vra4Template, true, "stylePeriod", strarrLangName);
        
        ////////////////// SUBJECT //////////////////
        strarrLangName = new String[][]{
            {"en","Subject"},
            {"de","Thema / Gegenstand"}
        };
        addMetadataSet(vra4Template, true, "subject", strarrLangName);
        
        ////////////////// TECHNIQUE //////////////////
        strarrLangName = new String[][]{
            {"en","Technique"},
            {"de","Technik / Methode"}
        };
        addMetadataSet(vra4Template, true, "technique", strarrLangName);
        
        ////////////////// TEXTREF //////////////////
        strarrLangName = new String[][]{
            {"en","Text Ref"},
            {"de","Text Ref"}
        };
        addMetadataSet(vra4Template, true, "textref", strarrLangName);
        
        ////////////////// TITLE //////////////////
        strarrLangName = new String[][]{
            {"en","Title"},
            {"de","Titel"}
        };
        addMetadataSet(vra4Template, true, "title", strarrLangName);
        
        ////////////////// WORK TYPE //////////////////
        strarrLangName = new String[][]{
            {"en","Work Type"},
            {"de","Werkart"}
        };
        addMetadataSet(vra4Template, true, "worktype", strarrLangName);
        
        return vra4Template;
    }

    
    /**
     * Elements and subelements may have the following global, optional attributes:
     * – dataDate
     * – extent
     * – href
     * – pref
     * – refid
     * – rules
     * – source
     * – vocab
     * – xml:lang
     * One display and one notes subelement may be added to any element set as needed.
     * 
     * Work in progress: this method is unfinished. ~HGK
     * 
     * @param strBaseEN
     * @param strBaseDE
     * @return 
     */
    private String[][] prepVRA4GlobalMetadata(String strBaseEN, String strBaseDE) {
        String[][] strarrLangName = new String[][]{
            {"en", strBaseEN + ".id"},
            {"de", strBaseDE + ".id"}
        };
        
        return strarrLangName;
    }
    
    
    public static HiFlexMetadataTemplate getDCTemplateBlueprint() {
        HiFlexMetadataTemplate dcTemplate = new HiFlexMetadataTemplate();
        dcTemplate.setNamespacePrefix("dc"); //$NON-NLS-1$
        dcTemplate.setNamespaceURI("http://purl.org/dc/elements/1.1/"); //$NON-NLS-1$
        dcTemplate.setNamespaceURL("http://purl.org/dc/elements/1.1/"); //$NON-NLS-1$

        HiFlexMetadataSet dcSet;
        HiFlexMetadataName dcName;

        // TODO refactor
        // source: http://dublincore.org/documents/dces/
        // german names source: http://www.kim-forum.org/material/pdf/uebersetzung_dcmes_20070822.pdf

        // contributor
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("contributor"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Mitwirkende / Mitwirkender"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("contributor"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // coverage
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("coverage"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Geltungsbereich"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("coverage"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // creator
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("creator"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Urheberin / Urheber"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("creator"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // date
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("date"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Zeitangabe"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("date"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // description
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("description"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Beschreibung"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("description"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // format
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("format"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Format"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("format"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // identifier
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("identifier"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Identifikator"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("identifier"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // language
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("language"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Sprache"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("language"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // publisher
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("publisher"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Verlegerin / Verleger"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("publisher"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // relation
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("relation"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Beziehung"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("relation"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // rights
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("rights"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Rechte"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("rights"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // source
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("source"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Quelle"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("source"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // subject
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("subject"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Thema"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("subject"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // title
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("title"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Titel"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("title"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);
        // type
        dcSet = new HiFlexMetadataSet();
        dcSet.setRichText(false);
        dcSet.setTagname("type"); //$NON-NLS-1$
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("Typ"); //$NON-NLS-1$
        dcName.setLanguage("de"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcName = new HiFlexMetadataName();
        dcName.setDisplayName("type"); //$NON-NLS-1$
        dcName.setLanguage("en"); //$NON-NLS-1$
        dcSet.getDisplayNames().add(dcName);
        dcTemplate.getEntries().add(dcSet);

        return dcTemplate;
    }


    public static HiFlexMetadataTemplate getCDWALiteTemplateBlueprint() {
        HiFlexMetadataTemplate cdwaTemplate = new HiFlexMetadataTemplate();
        cdwaTemplate.setNamespacePrefix("cdwalite"); //$NON-NLS-1$
        cdwaTemplate.setNamespaceURI("http://www.getty.edu/CDWA/CDWALite"); //$NON-NLS-1$
        cdwaTemplate.setNamespaceURL("http://www.getty.edu/CDWA/CDWALite/CDWALite-xsd-public-v1-1.xsd"); //$NON-NLS-1$

        HiFlexMetadataSet cdwaSet;
        HiFlexMetadataName cdwaName;

        // TODO refactor
        // source: http://www.getty.edu/research/conducting_research/standards/cdwa/cdwalite.pdf

        // objectWorkType
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("objectWorkType"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Objekt / Typ"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Object / Work Type"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // title
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("title"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Titel"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Title"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // displayCreator
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("displayCreator"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Anzeigedaten des Erstellers"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Display Creator"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // nameCreator
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("nameCreator"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Name des Erstellers"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Name of Creator"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // nationalityCreator
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("nationalityCreator"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Nationalität des Erstellers"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Nationality Creator"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // vitalDatesCreator
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("vitalDatesCreator"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Wichtige Vita-Daten des Erstellers"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Vital Dates Creator"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // roleCreator
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("roleCreator"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Rolle des Erstellers"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Role Creator"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // displayMeasurements
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("displayMeasurements"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Anzeige-Maße"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Display Measurements"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // displayMaterialsTech
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("displayMaterialsTech"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Materialien / Methoden"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Display Materials / Techniques"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // displayCreationDate
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("displayCreationDate"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Anzeige-Erstellungsdatum"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Display Creation Date"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // earliestDate
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("earliestDate"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Anfangsdatum"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Earliest Date"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // latestDate
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("latestDate"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Enddatum"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Latest Date"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // locationName
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("locationName"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Standort / Repository Name"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Location / Repository Name"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // workID
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("workID"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Repository Objekt ID"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Repository Work Identification Number"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // subjectTerm
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("subjectTerm"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Beschreibung / Begriffe des Werkinhaltes"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Indexing Subject Term"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // recordID
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("recordID"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Lokale Datensatz ID"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Record ID"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);
        // recordType
        cdwaSet = new HiFlexMetadataSet();
        cdwaSet.setRichText(false);
        cdwaSet.setTagname("recordType"); //$NON-NLS-1$
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Datensatz Typ"); //$NON-NLS-1$
        cdwaName.setLanguage("de"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaName = new HiFlexMetadataName();
        cdwaName.setDisplayName("Record Type"); //$NON-NLS-1$
        cdwaName.setLanguage("en"); //$NON-NLS-1$
        cdwaSet.getDisplayNames().add(cdwaName);
        cdwaTemplate.getEntries().add(cdwaSet);

        return cdwaTemplate;
    }



    public static HiFlexMetadataTemplate getCustomTemplateBlueprint() {
        HiFlexMetadataTemplate customTemplate = new HiFlexMetadataTemplate();
        customTemplate.setNamespacePrefix("custom"); //$NON-NLS-1$
        customTemplate.setNamespaceURI(""); //$NON-NLS-1$
        customTemplate.setNamespaceURL(""); //$NON-NLS-1$


        return customTemplate;
    }

    public static String getFuzzyDate(long timestamp) {
        String fuzzyDate = "";
        long now = new Date().getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now - timestamp);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now - timestamp);
        long hours = TimeUnit.MILLISECONDS.toHours(now - timestamp);
        long days = TimeUnit.MILLISECONDS.toDays(now - timestamp);
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        if ( years > 0 ) fuzzyDate = (years == 1) ? Messages.getString("MetadataHelper.oneYearAgo") : String.format(Messages.getString("MetadataHelper.yearsAgo"), years);
        else if ( months > 0 ) fuzzyDate = (months == 1) ? Messages.getString("MetadataHelper.oneMonthAgo") : String.format(Messages.getString("MetadataHelper.monthsAgo"), months);
        else if ( weeks > 0 ) fuzzyDate = (weeks == 1) ? Messages.getString("MetadataHelper.oneWeekAgo") : String.format(Messages.getString("MetadataHelper.weeksAgo"), weeks);
        else if ( days > 0 ) fuzzyDate = (days == 1) ? Messages.getString("MetadataHelper.oneDayAgo") : String.format(Messages.getString("MetadataHelper.daysAgo"), days);
        else if ( hours > 0 ) fuzzyDate = (hours == 1) ? Messages.getString("MetadataHelper.oneHourAgo") : String.format(Messages.getString("MetadataHelper.hoursAgo"), hours);
        else if ( minutes > 0 ) fuzzyDate = (minutes == 1) ? Messages.getString("MetadataHelper.oneMinuteAgo") : String.format(Messages.getString("MetadataHelper.minutesAgo"), minutes);
        else if ( seconds > 4 ) fuzzyDate = String.format(Messages.getString("MetadataHelper.secondsAgo"), seconds);
        else fuzzyDate = Messages.getString("MetadataHelper.now");        

        return fuzzyDate;
    }
}
