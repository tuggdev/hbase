/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.filter;

import com.google.protobuf.InvalidProtocolBufferException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.protobuf.generated.ComparatorProtos;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * A long comparator that compares long values compares against the specified byte array
 * with support for arbitrary precision, signed decimal values
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class LongComparator extends ByteArrayComparable {
	private BigDecimal decimalValue;
	private Locale locale;

	/**
	 * Creates comparator with a long value, setting the default locale
	 * @param value
	 */
    public LongComparator(long value) {
      super(Bytes.toBytes(value));
      decimalValue = new BigDecimal(value);
      locale = Locale.getDefault();
    }
    
    /***
     * Creates a comparator with a string value. If the value represents a decimal value, it will
     * be parsed into a BigDecimal using the default locale
     * 
     * @param value
     */
    public LongComparator(byte[] value) {
    	super(value);
    	locale = Locale.getDefault();
    	decimalValue = parse(new String(value));
    }

    @Override
    public int compareTo(byte[] value, int offset, int length) {
      try {	
    	  BigDecimal that = parse(Bytes.toString(value, offset, length));
          return this.decimalValue.compareTo(that);
      } catch (NumberFormatException e) {
    	  // if the input cells being compared are not a number, this method returns 1
    	  return 1;
      }
    }

    /**
     * @return The comparator serialized using pb
     */
    @Override
    public byte [] toByteArray() {
        ComparatorProtos.LongComparator.Builder builder =
                ComparatorProtos.LongComparator.newBuilder();
        builder.setComparable(super.convert());
        return builder.build().toByteArray();
    }

    /**
     * @param pbBytes A pb serialized {@link BinaryComparator} instance
     * @return An instance of {@link BinaryComparator} made from <code>bytes</code>
     * @throws org.apache.hadoop.hbase.exceptions.DeserializationException
     * @see #toByteArray
     */
    public static LongComparator parseFrom(final byte [] pbBytes)
            throws DeserializationException {
        ComparatorProtos.LongComparator proto;
        try {
            proto = ComparatorProtos.LongComparator.parseFrom(pbBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException(e);
        }
        return new LongComparator(proto.getComparable().getValue().toByteArray());
    }

    /**
     * @param other
     * @return true if and only if the fields of the comparator that are serialized
     * are equal to the corresponding fields in other.  Used for testing.
     */
    boolean areSerializedFieldsEqual(LongComparator other) {
        if (other == this) return true;
        return super.areSerializedFieldsEqual(other);
    }
    
    private BigDecimal parse(String str) {
        DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(locale);
        decimalFormat.setParseBigDecimal(true);

        return (BigDecimal)decimalFormat.parse(str, new ParsePosition(0));
    }
    
}
