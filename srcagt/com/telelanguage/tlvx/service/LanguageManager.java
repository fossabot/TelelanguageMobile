package com.telelanguage.tlvx.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.Language;

/**
 * LanguageManager
 */
public class LanguageManager
{
    private static final Logger LOG = Logger.getLogger(LanguageManager.class);

    public List<Language> findLanguages()
    {
        if (LOG.isDebugEnabled()) LOG.debug("findLanguages");
        List<Language> languages = TLVXManager.languageDAO.findLanguages();
        return languages;
    }
}
