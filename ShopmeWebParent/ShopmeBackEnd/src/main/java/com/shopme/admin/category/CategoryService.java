package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;

@Service
@Transactional
public class CategoryService {
	private static final int ROOT_CATEGORIES_PER_PAGE = 4;
	
	@Autowired
	private CategoryRepository repo;
	
	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir) {
		System.err.println("current page " + pageNum);
		Sort sort = Sort.by("name");
		
		 if (sortDir.equals("asc")) {
			sort = sort.ascending();
		} else if (sortDir.equals("desc")) {
			sort = sort.descending();
		}
		
		 Pageable pageable = PageRequest.of(pageNum-1 , ROOT_CATEGORIES_PER_PAGE, sort);
		 System.err.println(pageable);
		 Page<Category> pageCategories = repo.findRootCategories(pageable);
		 System.err.println("pageCategories " + pageCategories.getTotalElements());
		 System.err.println("pageCategories " + pageCategories.getTotalPages());
		 System.err.println("pageCategories " + pageCategories.getSize());
		 
		 List<Category> rootCategories = pageCategories.getContent();
		 System.err.println(rootCategories.size());
		 
		 pageInfo.setTotalElements(pageCategories.getTotalElements());
		 pageInfo.setTotalPages(pageCategories.getTotalPages());
		 
		 return listHirerarchicalCategories(rootCategories, sortDir);
	}
	
	private List<Category> listHirerarchicalCategories(List<Category> rootCategories, String sortDir) {
		List<Category> hierachicalCategories = new ArrayList<>();
		
		for (Category rootCategory : rootCategories) {
			hierachicalCategories.add(Category.copyFull(rootCategory));
			
//			Set<Category> children = rootCategory.getChildren();
			Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);
			
			for (Category subCategory : children) {
				String name = "--" + subCategory.getName();
				hierachicalCategories.add(Category.copyFull(subCategory, name));
				
				listSubHierachicalCategories(hierachicalCategories, subCategory, 1, sortDir);
			}
		}
		
		return hierachicalCategories;
	}
	
	private void listSubHierachicalCategories(List<Category> hierachicalCategories,
			Category parent, int subLevel, String sortDir) {
//		Set<Category> children = parent.getChildren();
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		
		for (Category subCategory : children) {
			String name = "";
			for (int i=0; i< newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			
			hierachicalCategories.add(Category.copyFull(subCategory, name));
			
			listSubHierachicalCategories(hierachicalCategories, subCategory, newSubLevel, sortDir);
		}
	}
	

	public Category save(Category category) {
		return repo.save(category);
	}
	
	public List<Category> listCategoriesUsedInForm() {
		List<Category> catetoriesUsedInForm = new ArrayList<>();
		
		Iterable<Category> categoriesInDB = repo.findRootCategories(Sort.by("name").ascending());
		
		for (Category category : categoriesInDB) {
			if (category.getParent() == null) {
				catetoriesUsedInForm.add(Category.copyIdAndName(category));
				
//				Set<Category> children = category.getChildren();
				Set<Category> children = sortSubCategories(category.getChildren());
				
				for (Category subCategory : children) {
					String name = "--" + subCategory.getName();
					catetoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					
					listSubCategoriesUsedInForm(catetoriesUsedInForm, subCategory, 1);
			 	}
		    }
	    }
		return catetoriesUsedInForm;
	}
	
	private void listSubCategoriesUsedInForm(List<Category> catetoriesUsedInForm, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
//		Set<Category> children = parent.getChildren();
		Set<Category> children = sortSubCategories(parent.getChildren());
		
		for (Category subCategory : children) {
			String name="";
			for (int i=0; i<newSubLevel; i++) {
				  name += "--";
			}
			name += subCategory.getName();
			
			catetoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			
			listSubCategoriesUsedInForm(catetoriesUsedInForm, subCategory, newSubLevel);
		    }
	}
	
	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new CategoryNotFoundException("Count not find any catetory with ID " + id);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		
		Category categoryByName = repo.findByName(name);
		
		if (isCreatingNew) {
			if (categoryByName != null) {
				return "DuplicateName";
			} else {
				Category categoryByAlias = repo.findByAlias(alias);
				if (categoryByAlias != null) {
					return "DuplicateAlias";
				}
			}
		} else {
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}
			
			Category categoryByAlias = repo.findByAlias(alias);
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}
		
		return "OK";
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children) {
		return sortSubCategories(children, "asc");
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {
			
			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
				 return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}
			}
		});
		
		sortedChildren.addAll(children);
		
		return sortedChildren;
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		System.err.println(id + " " + enabled);
		repo.updateEnabledStatus(id, enabled);
	}
	
	public void delete(Integer id) throws CategoryNotFoundException {
		Long countById = repo.countById(id);
		if (countById == null || countById == 0 ) {
			throw new CategoryNotFoundException("Could not find any category with ID " + id);
		}
		
		repo.deleteById(id);
	}
}
