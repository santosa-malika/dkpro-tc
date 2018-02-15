/*******************************************************************************
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.features.ngram;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.MaxNrOfTokensOverAllSentenceMC;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Ratio of the number of tokens in a document with respect to the longest document in the training data
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfTokensRatioPerSentence extends MaximunNormalizationExtractorBase {

	public static final String FEATURE_NAME = "NumberOfTokensPerSentenceRatio";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
			throws TextClassificationException {

		long maxLen = getMax();

		List<Sentence> sentences = JCasUtil.selectCovered(jcas, Sentence.class, classificationUnit);
		double ratio = getRatio(sentences.size(), maxLen);
		return new Feature(FEATURE_NAME, ratio, FeatureType.NUMERIC).asSet();
	}

	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
			throws ResourceInitializationException {

		return Arrays.asList(
				new MetaCollectorConfiguration(MaxNrOfTokensOverAllSentenceMC.class, parameterSettings)
						.addStorageMapping(MaxNrOfTokensOverAllSentenceMC.PARAM_TARGET_LOCATION,
								NrOfTokensRatioPerSentence.PARAM_SOURCE_LOCATION,
								MaxNrOfTokensOverAllSentenceMC.LUCENE_DIR));
	}

	@Override
	protected String getFieldName() {
		return MaxNrOfTokensOverAllSentenceMC.LUCENE_MAX_TOKEN_FIELD + featureExtractorName;
	}

	@Override
	protected String getFeaturePrefix() {
		return "maxTokenPerSentences";
	}

}