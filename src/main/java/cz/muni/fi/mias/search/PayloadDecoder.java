package cz.muni.fi.mias.search;

import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.util.BytesRef;

import static cz.muni.fi.mias.math.PayloadHelper.decodeShort;

/**
 *  Payload decoder for {@link PayloadScoreQuery} class.
 *
 *  Based on {@link cz.muni.fi.mias.math.PayloadHelper}.
 */
public class PayloadDecoder implements org.apache.lucene.queries.payloads.PayloadDecoder {

    /**
     * Converts byte array to float number
     */
    @Override
    public float computePayloadFactor(BytesRef payload) {
        float result = (float) decodeShort(payload.bytes) + 32768;
        return result / 10000;
    }
}
