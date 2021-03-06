package scala.collection


package object generic {
  @deprecated("Clearable was moved from collection.generic to collection.mutable", "2.13.0")
  type Clearable = scala.collection.mutable.Clearable

  @deprecated("Use scala.collection.BuildFrom instead", "2.13.0")
  type CanBuildFrom[-From, -A, +C] = scala.collection.BuildFrom[From, A, C]

  @deprecated("Growable was moved from collection.generic to collection.mutable", "2.13.0")
  type Growable[-A] = scala.collection.mutable.Growable[A]

  @deprecated("Shrinkable was moved from collection.generic to collection.mutable", "2.13.0")
  type Shrinkable[-A] = scala.collection.mutable.Shrinkable[A]

  @deprecated("Use IsIterable instead", "2.13.0")
  type IsTraversableLike[Repr] = IsIterable[Repr]

  @deprecated("Use IsIterableOnce instead", "2.13.0")
  type IsTraversableOnce[Repr] = IsIterableOnce[Repr]
}
