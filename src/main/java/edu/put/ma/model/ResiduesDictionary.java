package edu.put.ma.model;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.Residue;
import edu.put.ma.model.protein.Alanine;
import edu.put.ma.model.protein.AminoAcid;
import edu.put.ma.model.protein.AminoAcidType;
import edu.put.ma.model.protein.Arginine;
import edu.put.ma.model.protein.Asparagine;
import edu.put.ma.model.protein.AsparticAcid;
import edu.put.ma.model.protein.Cysteine;
import edu.put.ma.model.protein.GlutamicAcid;
import edu.put.ma.model.protein.Glutamine;
import edu.put.ma.model.protein.Glycine;
import edu.put.ma.model.protein.Histidine;
import edu.put.ma.model.protein.Isoleucine;
import edu.put.ma.model.protein.Leucine;
import edu.put.ma.model.protein.Lysine;
import edu.put.ma.model.protein.Methionine;
import edu.put.ma.model.protein.Phenylalanine;
import edu.put.ma.model.protein.Proline;
import edu.put.ma.model.protein.Serine;
import edu.put.ma.model.protein.Threonine;
import edu.put.ma.model.protein.Tryptophan;
import edu.put.ma.model.protein.Tyrosine;
import edu.put.ma.model.protein.Valine;
import edu.put.ma.model.rna.Adenine;
import edu.put.ma.model.rna.Cytosine;
import edu.put.ma.model.rna.Guanine;
import edu.put.ma.model.rna.Nucleotide;
import edu.put.ma.model.rna.NucleotideType;
import edu.put.ma.model.rna.Uracil;

public final class ResiduesDictionary {

    private static final ImmutableList<Nucleotide> NUCLEOTIDES_DICTIONARY = ImmutableList.of(
            (Nucleotide) new Adenine(), new Cytosine(), new Guanine(), new Uracil());

    private static final ImmutableMap<String, Integer> NUCLEOTIDES_DICTIONARY_ACCESS_MAP = getImmutableDictionaryMap(
            NUCLEOTIDES_DICTIONARY, MoleculeType.RNA);

    private static final ImmutableList<AminoAcid> AMINO_ACIDS_DICTIONARY = ImmutableList.of(
            (AminoAcid) new Alanine(), new Arginine(), new Asparagine(), new AsparticAcid(), new Cysteine(),
            new GlutamicAcid(), new Glutamine(), new Glycine(), new Histidine(), new Isoleucine(),
            new Leucine(), new Lysine(), new Methionine(), new Phenylalanine(), new Proline(), new Serine(),
            new Threonine(), new Tryptophan(), new Tyrosine(), new Valine());

    private static final ImmutableMap<String, Integer> AMINO_ACIDS_DICTIONARY_ACCESS_MAP = getImmutableDictionaryMap(
            AMINO_ACIDS_DICTIONARY, MoleculeType.PROTEIN);

    private ResiduesDictionary() {
        // hidden constructor
    }

    public static final Nucleotide construct(final NucleotideType nucleotideType) {
        Nucleotide nucleotide = null;
        switch (nucleotideType) {
            case A:
                nucleotide = new Adenine();
                break;
            case C:
                nucleotide = new Cytosine();
                break;
            case G:
                nucleotide = new Guanine();
                break;
            case U:
                nucleotide = new Uracil();
                break;
            default:
        }
        return nucleotide;
    }

    public static final AminoAcid construct(final AminoAcidType aminoAcidType) {
        AminoAcid result = null;
        result = firstConstruction(aminoAcidType);
        if (result == null) {
            result = secondConstruction(aminoAcidType);
            if (result == null) {
                result = thirdConstruction(aminoAcidType);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <T extends Residue> T getResidueEntry(final String residueName,
            final MoleculeType moleculeType) {
        if (moleculeType == MoleculeType.PROTEIN) {
            if (AMINO_ACIDS_DICTIONARY_ACCESS_MAP.containsKey(residueName)) {
                return (T) AMINO_ACIDS_DICTIONARY.get(AMINO_ACIDS_DICTIONARY_ACCESS_MAP.get(residueName));
            }
        } else {
            if (NUCLEOTIDES_DICTIONARY_ACCESS_MAP.containsKey(residueName)) {
                return (T) NUCLEOTIDES_DICTIONARY.get(NUCLEOTIDES_DICTIONARY_ACCESS_MAP.get(residueName));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <E> ImmutableList<E> getResidueDictionary(final MoleculeType moleculeType) {
        return (ImmutableList<E>) ((moleculeType == MoleculeType.PROTEIN) ? AMINO_ACIDS_DICTIONARY
                : NUCLEOTIDES_DICTIONARY);
    }

    public static final boolean considersAtom(final List<String> atomNames, final MoleculeType moleculeType) {
        final ImmutableList<? extends Residue> dictionary = getResidueDictionary(moleculeType);
        final int atomsCount = org.apache.commons.collections4.CollectionUtils.size(atomNames);
        int count = 0;
        for (String atomName : atomNames) {
            boolean result = false;
            for (Residue residue : dictionary) {
                if (residue.considersAtom(atomName)) {
                    result = true;
                    break;
                }
            }
            if (result) {
                count++;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Inproper atom name '%s' considered by in-contact residues expression", atomName));
            }
        }
        return atomsCount == count;
    }

    private static final ImmutableMap<String, Integer> getImmutableDictionaryMap(
            final ImmutableList<? extends Residue> dictionary, final MoleculeType moleculeType) {
        final Builder<String, Integer> builder = new ImmutableMap.Builder<String, Integer>();
        for (int i = 0; i < org.apache.commons.collections4.CollectionUtils.size(dictionary); i++) {
            builder.put((moleculeType == MoleculeType.PROTEIN) ? dictionary.get(i).getThreeLettersCode()
                    : dictionary.get(i).getSingleLetterCode(), i);
        }
        return builder.build();
    }

    private static final AminoAcid firstConstruction(final AminoAcidType aminoAcidType) {
        AminoAcid aminoAcid = null;
        switch (aminoAcidType) {
            case ALA:
                aminoAcid = new Alanine();
                break;
            case ARG:
                aminoAcid = new Arginine();
                break;
            case ASN:
                aminoAcid = new Asparagine();
                break;
            case ASP:
                aminoAcid = new AsparticAcid();
                break;
            case CYS:
                aminoAcid = new Cysteine();
                break;
            case GLU:
                aminoAcid = new GlutamicAcid();
                break;
            case GLN:
                aminoAcid = new Glutamine();
                break;
            default:
        }
        return aminoAcid;
    }

    private static final AminoAcid secondConstruction(final AminoAcidType aminoAcidType) {
        AminoAcid aminoAcid = null;
        switch (aminoAcidType) {
            case GLY:
                aminoAcid = new Glycine();
                break;
            case HIS:
                aminoAcid = new Histidine();
                break;
            case ILE:
                aminoAcid = new Isoleucine();
                break;
            case LEU:
                aminoAcid = new Leucine();
                break;
            case LYS:
                aminoAcid = new Lysine();
                break;
            case MET:
                aminoAcid = new Methionine();
                break;
            case PHE:
                aminoAcid = new Phenylalanine();
                break;
            default:
        }
        return aminoAcid;
    }

    private static final AminoAcid thirdConstruction(final AminoAcidType aminoAcidType) {
        AminoAcid aminoAcid = null;
        switch (aminoAcidType) {
            case PRO:
                aminoAcid = new Proline();
                break;
            case SER:
                aminoAcid = new Serine();
                break;
            case THR:
                aminoAcid = new Threonine();
                break;
            case TRP:
                aminoAcid = new Tryptophan();
                break;
            case TYR:
                aminoAcid = new Tyrosine();
                break;
            case VAL:
                aminoAcid = new Valine();
                break;
            default:
        }
        return aminoAcid;
    }
}
