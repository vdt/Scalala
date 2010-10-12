/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */
package scalala;
package tensor;

import domain._;
import generic._;

import scalala.generic.{CanAdd,CanSub,CanMul,CanDiv,CanPow,CanMod};
import scalala.generic.collection.{CanMapValues,CanMapKeyValuePairs};
import scalala.generic.tensor.CanJoinValues;

/**
 * Base companion methods for tensors that have a known key type,
 * such as Vectors (Int) and Matrices (Int,Int).  This class is
 * almost identical to TensorCompanion but Bound only specifies
 * a value type, not a key type.  There might be a cleverer way
 * to do this with type constructors and inheriting from TensorCompanion.
 *
 * @author dramage
 */
trait IndexedTensorCompanion[K,Bound[V]<:Tensor[K,V]] {

  implicit def canMapValues[V, RV:Scalar]
  : CanMapValues[Bound[V],V,RV,Bound[RV]]
  = new CanMapValues[Bound[V],V,RV,Bound[RV]] {
    override def map(from : Bound[V], fn : (V=>RV)) = {
      val builder = from.newBuilder[RV];
      from.foreach((k,v) => builder(k) = fn(v));
      builder.result.asInstanceOf[Bound[RV]];
    }
    override def mapNonZero(from : Bound[V], fn : (V=>RV)) = {
      val builder = from.newBuilder[RV];
      from.foreachNonZero((k,v) => builder(k) = fn(v));
      builder.result.asInstanceOf[Bound[RV]];
    }
  }

  implicit def canMapKeyValuePairs[V, RV:Scalar]
  : CanMapKeyValuePairs[Bound[V],K,V,RV,Bound[RV]]
  = new CanMapKeyValuePairs[Bound[V],K,V,RV,Bound[RV]] {
    override def map(from : Bound[V], fn : ((K,V)=>RV)) = {
      val builder = from.newBuilder[RV];
      from.foreach((k,v) => builder(k) = fn(k,v));
      builder.result.asInstanceOf[Bound[RV]];
    }
    override def mapNonZero(from : Bound[V], fn : ((K,V)=>RV)) = {
      val builder = from.newBuilder[RV];
      from.foreachNonZero((k,v) => builder(k) = fn(k,v));
      builder.result.asInstanceOf[Bound[RV]];
    }
  }

  implicit def canJoin[V1, V2, RV:Scalar]
  : CanJoinValues[Bound[V1], Tensor[K,V2], V1, V2, RV, Bound[RV]] =
  new CanJoinValues[Bound[V1], Tensor[K,V2], V1, V2, RV, Bound[RV]] {
    override def joinAll(a : Bound[V1], b : Tensor[K,V2], fn : (V1,V2)=>RV) = {
      a.checkDomain(b.domain);
      val builder = a.newBuilder[RV];
      a.foreach((k,aV) => builder(k) = fn(aV,b(k)));
      builder.result.asInstanceOf[Bound[RV]];
    }

    override def joinEitherNonZero(a : Bound[V1], b : Tensor[K,V2], fn : (V1,V2)=>RV) = {
      a.checkDomain(b.domain);
      val builder = a.newBuilder[RV];
      a.foreachNonZero((k,aV) => builder(k) = fn(aV,b(k)));
      b.foreachNonZero((k,bV) => builder(k) = fn(a(k),bV));
      builder.result.asInstanceOf[Bound[RV]];
    }

    override def joinBothNonZero(a : Bound[V1], b : Tensor[K,V2], fn : (V1,V2)=>RV) = {
      a.checkDomain(b.domain);
      val builder = a.newBuilder[RV];
      a.foreachNonZero((k,aV) => builder(k) = fn(aV,b(k)));
      builder.result.asInstanceOf[Bound[RV]];
    }
  }

  //
  // Tensor-scalar
  //

  implicit def canAddScalar[V,O,RV,That](implicit op : CanAdd[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], so : Scalar[O])
  : CanAdd[Bound[V],O,That] = new CanAdd[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b))(bf);
    }
  }

  implicit def canSubScalar[V,O,RV,That](implicit op : CanSub[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], so : Scalar[O])
  : CanSub[Bound[V],O,That] = new CanSub[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b))(bf);
    }
  }

  implicit def canMulScalar[V,O,RV,That](implicit op : CanMul[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], sb : Scalar[O])
  : CanMul[Bound[V],O,That] = new CanMul[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      if (sb.isNaN(b)) {
        a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b))(bf);
      } else {
        a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapNonZeroValues(v => op(v, b))(bf);
      }
    }
  }

  implicit def canDivScalar[V,O,RV,That](implicit op : CanDiv[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], sb : Scalar[O])
  : CanDiv[Bound[V],O,That] = new CanDiv[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      if (b == sb.zero || sb.isNaN(b)) {
        a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b));
      } else {
        a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapNonZeroValues(v => op(v, b));
      }
    }
  }

  implicit def canPowScalar[V,O,RV,That](implicit op : CanPow[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], so : Scalar[O])
  : CanPow[Bound[V],O,That] = new CanPow[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b))(bf);
    }
  }

  implicit def canModScalar[V,O,RV,That](implicit op : CanMod[V,O,RV], bf : CanMapValues[Bound[V],V,RV,That], so : Scalar[O])
  : CanMod[Bound[V],O,That] = new CanMod[Bound[V],O,That] {
    override def apply(a : Bound[V], b : O) = {
      a.asInstanceOf[TensorLike[K,V,_,Bound[V]]].mapValues(v => op(v, b))(bf);
    }
  }


  //
  // Tensor-Tensor
  //

  implicit def canAddBound[V,O,RV,That](implicit op : CanAdd[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanAdd[Bound[V],Bound[O],That] = new CanAdd[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike joinEitherNonZero bLike)(op)(bfLike);
    }
  }

  implicit def canSubBound[V,O,RV,That](implicit op : CanSub[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanSub[Bound[V],Bound[O],That] = new CanSub[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike joinEitherNonZero bLike)(op)(bfLike);
    }
  }

  // TODO: this could be faster via joinBothNonZero if we knew there were no NaN in b
  implicit def canMulBound[V,O,RV,That](implicit op : CanMul[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanMul[Bound[V],Bound[O],That] = new CanMul[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike joinEitherNonZero bLike)(op)(bfLike);
    }
  }

  implicit def canDivBound[V,O,RV,That](implicit op : CanDiv[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanDiv[Bound[V],Bound[O],That] = new CanDiv[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike join bLike)(op)(bfLike);
    }
  }

  implicit def canPowBound[V,O,RV,That](implicit op : CanPow[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanPow[Bound[V],Bound[O],That] = new CanPow[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike join bLike)(op)(bfLike);
    }
  }

  implicit def canModBound[V,O,RV,That](implicit op : CanMod[V,O,RV], bf : CanJoinValues[Bound[V],Bound[O],V,O,RV,That], so : Scalar[O])
  : CanMod[Bound[V],Bound[O],That] = new CanMod[Bound[V],Bound[O],That] {
    override def apply(a : Bound[V], b : Bound[O]) = {
      val aLike = a.asInstanceOf[Tensor[K,V]];
      val bLike = b.asInstanceOf[Tensor[K,O]];
      val bfLike = bf.asInstanceOf[CanJoinValues[Tensor[K,V],Tensor[K,O],V,O,RV,That]];
      (aLike join bLike)(op)(bfLike);
    }
  }
}
